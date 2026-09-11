import "dotenv/config";
import { OllamaProvider, resolveOllamaModel } from "./provider.js";
import { PerceptionSchema, validateDecision } from "./contracts.js";
const baseUrl = process.env.OLLAMA_BASE_URL ?? "http://127.0.0.1:11434";
const model = await resolveOllamaModel(baseUrl, process.env.OLLAMA_MODEL ?? "");
const perception = PerceptionSchema.parse({ agentId: "4bde43d4-cbe1-49d0-8a33-9b2e8004881d", name: "Smoke", epoch: 1, self: { position: { x: 0, y: 64, z: 0 }, health: 20, hunger: 20, heldItem: "minecraft:air" }, environment: { dimension: "minecraft:overworld", timeOfDay: 6000, weather: "clear", light: 15, biome: "minecraft:plains" }, entities: [], events: ["You have arrived."], candidates: [{ id: "wait:1", kind: "WAIT", description: "Wait briefly and observe." }] });
const started = performance.now(); const decision = await new OllamaProvider(baseUrl, model, process.env.OLLAMA_THINK === "true", Number(process.env.OLLAMA_REQUEST_TIMEOUT_MS ?? 120000), process.env.OLLAMA_KEEP_ALIVE ?? "15m", Number(process.env.OLLAMA_NUM_CTX ?? 8192)).decide(perception, []); validateDecision(decision, perception);
console.log(JSON.stringify({ model, latencyMs: Math.round(performance.now() - started), decision, schemaValid: true }, null, 2));
