import { appendFile, mkdir, readFile, rename, writeFile } from "node:fs/promises";
import { join } from "node:path";
import type { EmbodimentEvent, Perception } from "./contracts.js";

export type EventMetadata={worldTime?:number;dimension?:string;position?:{x:number;y:number;z:number};actionKind?:string;blockId?:string;itemId?:string;entityId?:string;targetAgentId?:string;targetName?:string;success?:boolean};
export type EpisodicFact={schemaVersion?:1;id?:string;at:string;epoch:number;kind:"ACTION"|"OUTCOME"|"DEATH"|"SPEECH"|"DAMAGE"|"ACQUISITION";text:string;metadata?:EventMetadata;salience?:number;relativeTime?:string};
export type Reflection={at:string;text:string;salience?:number};
export type SubjectiveState={goals?:string[];intent:string;selfDescription?:string;preferences?:string[];concerns?:string[];aspirations?:string[];updatedAt:string};
export type Project={id:string;title:string;purpose:string;status:"ACTIVE"|"COMPLETED"|"ABANDONED"|"PAUSED";createdAt:string;lastUpdatedAt:string;progress:string};
export type Belief={id:string;belief:string;confidence:number;createdAt:string;updatedAt:string};
export type KnowledgeEntry={id:string;firstSeen:string;lastSeen:string;encounters:number;dimension?:string;position?:{x:number;y:number;z:number};name?:string};
export type Knowledge={dimensions:Record<string,KnowledgeEntry>;biomes:Record<string,KnowledgeEntry>;entityTypes:Record<string,KnowledgeEntry>;blockTypes:Record<string,KnowledgeEntry>;itemTypes:Record<string,KnowledgeEntry>;people:Record<string,KnowledgeEntry>};
export type Chapter={id:string;startedAt:string;endedAt:string;summary:string;topics:string[];people:string[];sourceStart:number;sourceEnd:number;createdAt:string};
export type MemoryMeta={schemaVersion:1;consolidatedUntil:number};
export type MemoryContext={working:EpisodicFact[];recalled:EpisodicFact[];social:EpisodicFact[];knowledge:string[];beliefs:Belief[];reflections:Reflection[];projects:Project[];chapters:Chapter[];self:SubjectiveState;contextChars:number};

const state=():SubjectiveState=>({goals:[],intent:"",updatedAt:new Date(0).toISOString()});
const knowledge=():Knowledge=>({dimensions:{},biomes:{},entityTypes:{},blockTypes:{},itemTypes:{},people:{}});
const salience=(kind:EpisodicFact["kind"])=>kind==="DEATH"?1:kind==="DAMAGE"?0.8:kind==="SPEECH"?0.65:kind==="ACQUISITION"?0.55:kind==="OUTCOME"?0.35:0.25;
const clamp=(n:number)=>Math.max(0,Math.min(1,n));
const distance=(a:{x:number;y:number;z:number},b:{x:number;y:number;z:number})=>Math.hypot(a.x-b.x,a.y-b.y,a.z-b.z);
const clip=(value:string,limit:number)=>value.length<=limit?value:`${value.slice(0,Math.max(0,limit-1))}…`;
const words=(value:string,limit=48)=>value.toLowerCase().match(/[\p{L}\p{N}_:-]{3,}/gu)?.slice(0,limit)??[];
const wallRelative=(time:string)=>{const parsed=Date.parse(time),minutes=Number.isNaN(parsed)?0:Math.max(0,Math.floor((Date.now()-parsed)/60000));return minutes<60?`${minutes} minutes ago`:`${Math.floor(minutes/60)} hours ago`;};
const minecraftRelative=(worldTime:number,referenceWorldTime:number)=>{const days=Math.max(0,Math.floor((referenceWorldTime-worldTime)/24000));return days===0?"same Minecraft day":days===1?"1 Minecraft day ago":`${days} Minecraft days ago`;};
const eventRelative=(fact:EpisodicFact,referenceWorldTime?:number)=>fact.metadata?.worldTime!==undefined&&referenceWorldTime!==undefined?minecraftRelative(fact.metadata.worldTime,referenceWorldTime):wallRelative(fact.at);
const present=(fact:EpisodicFact,referenceWorldTime?:number):EpisodicFact=>({...fact,relativeTime:eventRelative(fact,referenceWorldTime)});
const measure=(context:MemoryContext)=>{let size=JSON.stringify({...context,contextChars:0}).length;for(let index=0;index<3;index++){const next=JSON.stringify({...context,contextChars:size}).length;if(next===size)return size;size=next;}return size;};

/** Per-agent durable memory; subjective state is only a retrieval cue, never world evidence. */
export class MemoryStore {
  private readonly episodeCache=new Map<string,EpisodicFact[]>();
  constructor(private readonly root:string){}
  private dir(id:string){return join(this.root,id);} private path(id:string,file:string){return join(this.dir(id),file);}
  private async json<T>(id:string,file:string,fallback:T):Promise<T>{try{return JSON.parse(await readFile(this.path(id,file),"utf8")) as T;}catch{return fallback;}}
  private async atomic(id:string,file:string,value:unknown){await mkdir(this.dir(id),{recursive:true});const target=this.path(id,file),temp=`${target}.tmp`;await writeFile(temp,JSON.stringify(value,null,2),"utf8");await rename(temp,target);}
  async readEpisodes(id:string,limit=12){let all=this.episodeCache.get(id);if(!all){try{all=(await readFile(this.path(id,"episodes.jsonl"),"utf8")).split("\n").filter(Boolean).flatMap((line,index)=>{try{const raw=JSON.parse(line) as EpisodicFact;return[{...raw,id:raw.id??`legacy-${index}`,schemaVersion:1,salience:clamp(raw.salience??salience(raw.kind))}];}catch{return[];}});}catch{all=[];}this.episodeCache.set(id,all);}return limit>=all.length?[...all]:all.slice(-limit);}
  async appendEpisode(id:string,fact:EpisodicFact){const episodes=await this.readEpisodes(id,Number.MAX_SAFE_INTEGER);const next={...fact,schemaVersion:1 as const,id:fact.id??`${Date.now()}-${episodes.length}`,salience:clamp(fact.salience??salience(fact.kind))};const prior=episodes.at(-1);if(prior&&prior.kind===next.kind&&prior.text===next.text&&next.salience<=0.3)return;await mkdir(this.dir(id),{recursive:true});await appendFile(this.path(id,"episodes.jsonl"),JSON.stringify(next)+"\n","utf8");episodes.push(next);this.episodeCache.set(id,episodes);}
  async appendEmbodiment(id:string,event:EmbodimentEvent){await this.appendEpisode(id,{at:new Date().toISOString(),epoch:event.epoch,kind:event.kind,text:event.text,metadata:event.metadata,salience:salience(event.kind)});}
  async appendReflection(id:string,value:Reflection){await mkdir(this.dir(id),{recursive:true});await appendFile(this.path(id,"reflections.jsonl"),JSON.stringify(value)+"\n","utf8");}
  async readReflections(id:string,limit=4){try{return(await readFile(this.path(id,"reflections.jsonl"),"utf8")).split("\n").filter(Boolean).slice(-limit).flatMap(line=>{try{return[JSON.parse(line) as Reflection];}catch{return[];}});}catch{return[];}}
  async readState(id:string){const value=await this.json(id,"self.json",state());return{...state(),...value,goals:value.goals??[],preferences:value.preferences??[],concerns:value.concerns??[],aspirations:value.aspirations??[]};}
  async writeState(id:string,value:SubjectiveState){await this.atomic(id,"self.json",value);}
  async projects(id:string){return this.json<Project[]>(id,"projects.json",[]);} async writeProjects(id:string,value:Project[]){await this.atomic(id,"projects.json",value.slice(0,12));}
  async beliefs(id:string){return this.json<Belief[]>(id,"beliefs.json",[]);} async writeBeliefs(id:string,value:Belief[]){await this.atomic(id,"beliefs.json",value.slice(0,16));}
  async knowledge(id:string){return{...knowledge(),...(await this.json<Partial<Knowledge>>(id,"knowledge.json",{}))};} async writeKnowledge(id:string,value:Knowledge){await this.atomic(id,"knowledge.json",value);}
  async chapters(id:string,limit=8){try{return(await readFile(this.path(id,"autobiography.jsonl"),"utf8")).split("\n").filter(Boolean).slice(-limit).flatMap(line=>{try{return[JSON.parse(line) as Chapter];}catch{return[];}});}catch{return[];}}
  async appendChapter(id:string,value:Chapter){await mkdir(this.dir(id),{recursive:true});await appendFile(this.path(id,"autobiography.jsonl"),JSON.stringify(value)+"\n","utf8");}
  async meta(id:string){return this.json<MemoryMeta>(id,"memory-meta.json",{schemaVersion:1,consolidatedUntil:0});} async writeMeta(id:string,value:MemoryMeta){await this.atomic(id,"memory-meta.json",value);}
  async observe(perception:Perception){const now=new Date().toISOString(),all=await this.knowledge(perception.agentId);const hit=(bucket:Record<string,KnowledgeEntry>,id:string,extra:Partial<KnowledgeEntry>={})=>{const old=bucket[id];bucket[id]={...old,...extra,id,firstSeen:old?.firstSeen??now,lastSeen:now,encounters:(old?.encounters??0)+1};};const local={dimension:perception.environment.dimension,position:perception.self.position};hit(all.dimensions,perception.environment.dimension,local);hit(all.biomes,perception.environment.biome,local);for(const block of perception.blocks)hit(all.blockTypes,block.type,{dimension:local.dimension,position:block.position});for(const item of perception.items)hit(all.itemTypes,item.item,local);for(const entity of perception.entities){hit(all.entityTypes,entity.type,local);if(entity.name)hit(all.people,entity.emergentCraft?.agentId??`name:${entity.name}`,{name:entity.name,dimension:local.dimension,position:{x:local.position.x+entity.relativePosition.x,y:local.position.y+entity.relativePosition.y,z:local.position.z+entity.relativePosition.z}});}await this.writeKnowledge(perception.agentId,all);}
  async context(perception:Perception,budget=6000):Promise<MemoryContext>{
    const [episodes,reflections,self,allProjects,allBeliefs,allKnowledge,allChapters]=await Promise.all([this.readEpisodes(perception.agentId,Number.MAX_SAFE_INTEGER),this.readReflections(perception.agentId,8),this.readState(perception.agentId),this.projects(perception.agentId),this.beliefs(perception.agentId),this.knowledge(perception.agentId),this.chapters(perception.agentId,Number.MAX_SAFE_INTEGER)]);
    const activeProjects=allProjects.filter(project=>project.status==="ACTIVE"||project.status==="PAUSED").slice(0,3);
    // Subjective text below only selects factual records. It is never written to episodes or knowledge.
    const factualTerms=[perception.environment.dimension,perception.environment.biome,...perception.entities.flatMap(entity=>[entity.type,entity.name??""]),...perception.blocks.map(block=>block.type),...perception.items.map(item=>item.item)];
    const cueText=[...activeProjects.flatMap(project=>[project.title,project.purpose,project.progress]),self.intent,self.selfDescription??"",...(self.preferences??[]),...(self.concerns??[]),...(self.aspirations??[]),...allBeliefs.slice(0,6).map(belief=>belief.belief)].join(" ");
    const terms=new Set([...factualTerms.flatMap(value=>words(value)),...words(cueText,120)]);
    const referenceWorldTime=episodes.map(fact=>fact.metadata?.worldTime).filter((value):value is number=>value!==undefined).at(-1);
    const working=episodes.slice(-6);
    const ranked=episodes.map((fact,index)=>{const hay=`${fact.text} ${JSON.stringify(fact.metadata??{})}`.toLowerCase();const overlap=[...terms].filter(term=>hay.includes(term)).length;const social=perception.entities.some(entity=>entity.name&&hay.includes(entity.name.toLowerCase()))?2:0;const nearby=fact.metadata?.dimension===perception.environment.dimension&&fact.metadata.position&&distance(fact.metadata.position,perception.self.position)<32?1:0;const recency=(index+1)/Math.max(1,episodes.length);return{fact,score:overlap*3+social+nearby+(fact.salience??0)*2+recency,index};}).sort((a,b)=>b.score-a.score||b.index-a.index);
    const social=ranked.filter(item=>perception.entities.some(entity=>entity.name&&item.fact.text.toLowerCase().includes(entity.name.toLowerCase()))).slice(0,4).map(item=>present(item.fact,referenceWorldTime));
    const recalled=ranked.filter(item=>!working.includes(item.fact)&&!social.some(selected=>selected.id===item.fact.id)).slice(0,8).map(item=>present(item.fact,referenceWorldTime));
    const known=[...Object.values(allKnowledge.people),...Object.values(allKnowledge.entityTypes),...Object.values(allKnowledge.blockTypes),...Object.values(allKnowledge.itemTypes),...Object.values(allKnowledge.biomes)].filter(item=>terms.has(item.id.toLowerCase())||(item.name!==undefined&&terms.has(item.name.toLowerCase()))).slice(0,10).map(item=>`${item.name??item.id}: first seen ${wallRelative(item.firstSeen)}, last seen ${wallRelative(item.lastSeen)}${item.dimension?` in ${item.dimension}`:""}`);
    const chapters=allChapters.map((chapter,index)=>{const hay=`${chapter.summary} ${chapter.topics.join(" ")} ${chapter.people.join(" ")}`.toLowerCase(),overlap=[...terms].filter(term=>hay.includes(term)).length,recency=(index+1)/Math.max(1,allChapters.length);return{chapter,score:overlap*3+recency,index};}).sort((a,b)=>b.score-a.score||b.index-a.index).slice(0,3).map(item=>item.chapter);
    const result:MemoryContext={working:[],recalled:[],social:[],knowledge:[],beliefs:[],reflections:[],projects:[],chapters:[],self:{intent:"",updatedAt:""},contextChars:0};
    const fits=(candidate:MemoryContext)=>measure(candidate)<=budget;
    const add=<T>(source:T[],key:"working"|"recalled"|"social"|"knowledge"|"beliefs"|"reflections"|"projects"|"chapters",prepare:(item:T)=>T=item=>item)=>{for(const item of source){const candidate=structuredClone(result) as MemoryContext;const next=prepare(item);(candidate[key] as T[]).push(next);if(!fits(candidate)) break;(result[key] as T[]).push(next);}};
    const selfCandidate:SubjectiveState={goals:self.goals?.slice(0,3).map(value=>clip(value,120)),intent:clip(self.intent,180),updatedAt:self.updatedAt,selfDescription:self.selfDescription?clip(self.selfDescription,240):undefined,preferences:self.preferences?.slice(0,3).map(value=>clip(value,80)),concerns:self.concerns?.slice(0,3).map(value=>clip(value,80)),aspirations:self.aspirations?.slice(0,3).map(value=>clip(value,80))};
    if(fits({...result,self:selfCandidate})) result.self=selfCandidate;
    add(activeProjects,"projects",project=>({...project,title:clip(project.title,100),purpose:clip(project.purpose,180),progress:clip(project.progress,180)}));
    add(chapters,"chapters",chapter=>({...chapter,summary:clip(chapter.summary,360),topics:chapter.topics.slice(0,6).map(topic=>clip(topic,60)),people:chapter.people.slice(0,6).map(person=>clip(person,60))}));
    add(social,"social"); add(recalled,"recalled"); add(working.map(fact=>present(fact,referenceWorldTime)),"working"); add(known,"knowledge"); add(allBeliefs.slice(0,6),"beliefs",belief=>({...belief,belief:clip(belief.belief,200)})); add(reflections.slice(-4),"reflections",reflection=>({...reflection,text:clip(reflection.text,240)}));
    result.contextChars=measure(result);return result;
  }
}
