import { mkdir, readFile, rename, writeFile } from "node:fs/promises";
import { dirname, join } from "node:path";
import type { ModelProfileId } from "./profiles.js";

export type ProviderKind="ollama"|"openai-compatible"|"mock";
export type AgentModelConfig={provider:ProviderKind;model:string;profile:ModelProfileId};
export class ModelConfigStore {
  private configs:Record<string,AgentModelConfig>={}; private loaded=false;
  constructor(private readonly root:string,private readonly fallback:AgentModelConfig){}
  private get file(){return join(this.root,"agent-models.json");}
  async load(){if(this.loaded)return;this.loaded=true;try{this.configs=JSON.parse(await readFile(this.file,"utf8")) as Record<string,AgentModelConfig>;}catch{/* absent is normal */}}
  async get(agentId:string){await this.load();return this.configs[agentId]??this.fallback;}
  async set(agentId:string,next:Partial<AgentModelConfig>){await this.load();const value={...(this.configs[agentId]??this.fallback),...next};this.configs[agentId]=value;await mkdir(dirname(this.file),{recursive:true});const temporary=this.file+".tmp";await writeFile(temporary,JSON.stringify(this.configs,null,2)+"\n");await rename(temporary,this.file);return value;}
}
