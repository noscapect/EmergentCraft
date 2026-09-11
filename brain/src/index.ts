import "dotenv/config";
import { resolve } from "node:path";
import { createBrainServer } from "./service.js";
import { MemoryStore } from "./memory.js";
import { DeterministicProvider, OllamaProvider, resolveOllamaModel } from "./provider.js";
import { AsyncQueue } from "./queue.js";

const cfg = { host: process.env.HOST ?? "127.0.0.1", port: Number(process.env.PORT ?? 3847), baseUrl: process.env.OLLAMA_BASE_URL ?? "http://127.0.0.1:11434", model: process.env.OLLAMA_MODEL ?? "", think: process.env.OLLAMA_THINK === "true", timeout: Number(process.env.OLLAMA_REQUEST_TIMEOUT_MS ?? 120000), keepAlive: process.env.OLLAMA_KEEP_ALIVE ?? "15m", numCtx: Number(process.env.OLLAMA_NUM_CTX ?? 8192), provider: process.env.EC_PROVIDER ?? "ollama", maxConcurrent: Number(process.env.OLLAMA_MAX_CONCURRENT ?? 1) };
const provider = cfg.provider === "mock" ? new DeterministicProvider() : new OllamaProvider(cfg.baseUrl, await resolveOllamaModel(cfg.baseUrl, cfg.model), cfg.think, cfg.timeout, cfg.keepAlive, cfg.numCtx);
createBrainServer(provider, new MemoryStore(resolve(process.env.EC_MEMORY_DIR ?? "data")), new AsyncQueue(cfg.maxConcurrent)).listen(cfg.port, cfg.host, () => console.log(`[EmergentCraft Brain] listening on http://${cfg.host}:${cfg.port} (${cfg.provider}, max ${cfg.maxConcurrent})`));
