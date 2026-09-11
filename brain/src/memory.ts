import { appendFile, mkdir, readFile } from "node:fs/promises";
import { join } from "node:path";

export type EpisodicFact = { at: string; epoch: number; kind: "OBSERVATION" | "ACTION" | "OUTCOME" | "DEATH" | "SPEECH"; text: string };
export type SubjectiveState = { goals: string[]; intent: string; selfDescription?: string; updatedAt: string };
export type Reflection = { at: string; text: string };
export class MemoryStore {
  constructor(private readonly root: string) {}
  private dir(id: string) { return join(this.root, id); }
  async appendEpisode(id: string, fact: EpisodicFact) { await mkdir(this.dir(id), { recursive: true }); await appendFile(join(this.dir(id), "episodes.jsonl"), JSON.stringify(fact) + "\n", "utf8"); }
  async appendReflection(id: string, reflection: Reflection) { await mkdir(this.dir(id), { recursive: true }); await appendFile(join(this.dir(id), "reflections.jsonl"), JSON.stringify(reflection) + "\n", "utf8"); }
  async readEpisodes(id: string, limit = 12): Promise<EpisodicFact[]> { try { const lines = (await readFile(join(this.dir(id), "episodes.jsonl"), "utf8")).trim().split("\n").filter(Boolean); return lines.slice(-limit).map(line => JSON.parse(line) as EpisodicFact); } catch { return []; } }
  async readState(id: string): Promise<SubjectiveState> { try { return JSON.parse(await readFile(join(this.dir(id), "self.json"), "utf8")) as SubjectiveState; } catch { return { goals: [], intent: "", updatedAt: new Date(0).toISOString() }; } }
  async writeState(id: string, state: SubjectiveState) { await mkdir(this.dir(id), { recursive: true }); const { writeFile } = await import("node:fs/promises"); await writeFile(join(this.dir(id), "self.json"), JSON.stringify(state, null, 2), "utf8"); }
}

