import test from "node:test";
import assert from "node:assert/strict";
import { once } from "node:events";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { createBrainServer } from "../src/service.js";
import { MemoryStore } from "../src/memory.js";
import type { CognitionContext, CognitionProvider } from "../src/provider.js";
import type { Decision, Perception } from "../src/contracts.js";

const agentId = "4bde43d4-cbe1-49d0-8a33-9b2e8004881d";
const perception = { agentId, name: "Memory", epoch: 2, self: { position: { x: 0, y: 64, z: 0 }, health: 20, hunger: 20, heldItem: "minecraft:air" }, environment: { dimension: "minecraft:overworld", timeOfDay: 0, weather: "clear", light: 10, biome: "minecraft:plains" }, entities: [], events: [], candidates: [{ id: "wait:2", kind: "WAIT", description: "Wait." }] } as const;

class CapturingProvider implements CognitionProvider {
  context?: CognitionContext;
  async decide(input: Perception, context: CognitionContext): Promise<Decision> { this.context = context; return { actionId: input.candidates[0].id, intent: "continue", goal: null, reflection: null, speech: null }; }
}

test("embodiment outcome is persisted as factual episodic memory", async () => {
  const store = new MemoryStore(await mkdtemp(join(tmpdir(), "ec-event-")));
  const server = createBrainServer(new CapturingProvider(), store); server.listen(0, "127.0.0.1"); await once(server, "listening");
  try {
    const port = (server.address() as { port: number }).port;
    const response = await fetch(`http://127.0.0.1:${port}/v1/event`, { method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify({ agentId, epoch: 2, kind: "OUTCOME", text: "Reached the destination." }) });
    assert.equal(response.status, 204);
    assert.equal((await store.readEpisodes(agentId))[0].kind, "OUTCOME");
  } finally { server.close(); await once(server, "close"); }
});

test("self-authored goals and reflections enter later cognition context without becoming facts", async () => {
  const store = new MemoryStore(await mkdtemp(join(tmpdir(), "ec-context-")));
  await store.writeState(agentId, { goals: ["Understand this place."], intent: "Look around.", updatedAt: "now" });
  await store.appendReflection(agentId, { at: "now", text: "The horizon seems open." });
  await store.appendEpisode(agentId, { at: "now", epoch: 1, kind: "OUTCOME", text: "Waited." });
  const provider = new CapturingProvider(); const server = createBrainServer(provider, store); server.listen(0, "127.0.0.1"); await once(server, "listening");
  try {
    const port = (server.address() as { port: number }).port;
    const response = await fetch(`http://127.0.0.1:${port}/v1/decide`, { method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify(perception) });
    assert.equal(response.status, 200);
    assert.deepEqual(provider.context?.selfState.goals, ["Understand this place."]);
    assert.deepEqual(provider.context?.reflections, ["The horizon seems open."]);
    assert.ok(provider.context?.episodicFacts.includes("Waited."));
    assert.ok(!provider.context?.episodicFacts.includes("The horizon seems open."));
  } finally { server.close(); await once(server, "close"); }
});
