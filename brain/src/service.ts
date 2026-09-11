import { createServer, type IncomingMessage, type ServerResponse } from "node:http";
import { PerceptionSchema, validateDecision } from "./contracts.js";
import type { CognitionProvider } from "./provider.js";
import { MemoryStore } from "./memory.js";

async function body(request: IncomingMessage): Promise<unknown> { let text = ""; for await (const chunk of request) text += chunk; return JSON.parse(text); }
export function createBrainServer(provider: CognitionProvider, memory: MemoryStore) {
  return createServer(async (request: IncomingMessage, response: ServerResponse) => {
    if (request.method === "GET" && request.url === "/health") { response.writeHead(200, { "content-type": "application/json" }); response.end('{"ok":true}'); return; }
    if (request.method !== "POST" || request.url !== "/v1/decide") { response.writeHead(404); response.end(); return; }
    try {
      const perception = PerceptionSchema.parse(await body(request));
      await memory.appendEpisode(perception.agentId, { at: new Date().toISOString(), epoch: perception.epoch, kind: "OBSERVATION", text: `Perceived ${perception.entities.length} entities and ${perception.candidates.length} actions.` });
      const state = await memory.readState(perception.agentId);
      const episodic = (await memory.readEpisodes(perception.agentId)).map(event => event.text);
      const decision = validateDecision(await provider.decide(perception, episodic), perception);
      if (decision.reflection) await memory.appendReflection(perception.agentId, { at: new Date().toISOString(), text: decision.reflection });
      await memory.writeState(perception.agentId, { goals: decision.goal ? [decision.goal] : state.goals, intent: decision.intent, updatedAt: new Date().toISOString() });
      response.writeHead(200, { "content-type": "application/json" }); response.end(JSON.stringify(decision));
    } catch (error) { response.writeHead(400, { "content-type": "application/json" }); response.end(JSON.stringify({ error: error instanceof Error ? error.message : "Invalid request" })); }
  });
}

