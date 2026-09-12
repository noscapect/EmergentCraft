import { mkdir, readFile, rename, writeFile } from "node:fs/promises";
import { dirname, join } from "node:path";
import { z } from "zod";
import { profiles, type ModelProfileId } from "./profiles.js";
export const ProviderKindSchema=z.enum(["ollama","openai-compatible","mock"]); export type ProviderKind=z.infer<typeof ProviderKindSchema>;
export const AgentModelConfigSchema=z.object({provider:ProviderKindSchema,model:z.string().trim().min(1).max(200),profile:z.enum(["generic","qwen","minecraft-andy4","minecraft-andy41"])}).strict();
export type AgentModelConfig=z.infer<typeof AgentModelConfigSchema>;
export const BrainConfigPatchSchema=z.object({agentId:z.string().uuid(),provider:ProviderKindSchema.optional(),model:z.string().trim().min(1).max(200).optional(),profile:z.enum(["generic","qwen","minecraft-andy4","minecraft-andy41"]).optional()}).strict().refine(value=>value.provider!==undefined||value.model!==undefined||value.profile!==undefined,"at least one setting is required");
export class ModelConfigStore { private configs:Record<string,AgentModelConfig>={}; private loaded=false; constructor(private readonly root:string,private readonly fallback:AgentModelConfig){this.fallback=AgentModelConfigSchema.parse(fallback);} private get file(){return join(this.root,"agent-models.json");}
  async load(){if(this.loaded)return;this.loaded=true;try{this.configs=z.record(AgentModelConfigSchema).parse(JSON.parse(await readFile(this.file,"utf8")));}catch{/* absent/corrupt config does not corrupt memory */}}
  async get(agentId:string){z.string().uuid().parse(agentId);await this.load();return this.configs[agentId]??this.fallback;}
  async set(agentId:string,next:Partial<AgentModelConfig>){z.string().uuid().parse(agentId);await this.load();const value=AgentModelConfigSchema.parse({...this.configs[agentId]??this.fallback,...next});this.configs[agentId]=value;await mkdir(dirname(this.file),{recursive:true});const temporary=this.file+".tmp";await writeFile(temporary,JSON.stringify(this.configs,null,2)+"\n");await rename(temporary,this.file);return value;}
}
