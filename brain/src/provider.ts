import { DecisionSchema, type Decision, type Perception } from "./contracts.js";

export interface CognitionProvider { decide(perception: Perception, memory: string[]): Promise<Decision>; }
export class DeterministicProvider implements CognitionProvider {
  async decide(perception: Perception, _memory: string[] = []): Promise<Decision> { return { actionId: perception.candidates[0].id, intent: "I will pause and observe.", goal: null, reflection: null, speech: null }; }
}
export class OllamaProvider implements CognitionProvider {
  constructor(private readonly baseUrl: string, private readonly model: string, private readonly think: boolean, private readonly timeoutMs: number, private readonly keepAlive: string, private readonly numCtx: number) {}
  async decide(perception: Perception, memory: string[]): Promise<Decision> {
    const prompt = JSON.stringify({ instruction: "You are a person who exists in this world. You have no assigned mission. Decide for yourself what matters using only this perception, memories, and offered actions. Select exactly one actionId. Do not claim actions happened.", perception, recentEpisodicFacts: memory });
    const response = await fetch(`${this.baseUrl}/api/generate`, { method: "POST", signal: AbortSignal.timeout(this.timeoutMs), headers: { "content-type": "application/json" }, body: JSON.stringify({ model: this.model, prompt, stream: false, think: this.think, keep_alive: this.keepAlive, options: { num_ctx: this.numCtx }, format: { type: "object", properties: { actionId: { type: "string" }, intent: { type: "string" }, goal: { type: ["string", "null"] }, reflection: { type: ["string", "null"] }, speech: { type: ["string", "null"] } }, required: ["actionId", "intent", "goal", "reflection", "speech"], additionalProperties: false } }) });
    if (!response.ok) throw new Error(`Ollama ${response.status}: ${await response.text()}`);
    const body = await response.json() as { response: string };
    return DecisionSchema.parse(JSON.parse(body.response));
  }
}
export async function resolveOllamaModel(baseUrl: string, requested: string): Promise<string> { if (requested) return requested; const response = await fetch(`${baseUrl}/api/tags`); if (!response.ok) throw new Error("Ollama model listing failed"); const tags = await response.json() as { models?: { name: string }[] }; const model = tags.models?.[0]?.name; if (!model) throw new Error("No Ollama model installed; set OLLAMA_MODEL after pulling one"); return model; }
