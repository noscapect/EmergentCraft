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
const perception = { schemaVersion:1, agentId, name: "Memory", epoch: 2, capabilities:{playerHunger:false,inventory:true,nativeNavigation:true,blockBreaking:true,itemPickup:true}, self: { position: { x: 0, y: 64, z: 0 }, health: 20,maxHealth:20,alive:true,onGround:true,inWater:false,onFire:false,airSupply:300,fallDistance:0,effects:[],inventory:[] }, environment: { dimension: "minecraft:overworld", timeOfDay: 0, weather: "clear", light: 10, biome: "minecraft:plains",daytime:true,temperature:0.8,downfall:0 }, entities: [], blocks:[],items:[],events: [], candidates: [{ id: "wait:2", kind: "WAIT", description: "Wait." }] } as const;

class CapturingProvider implements CognitionProvider {
  context?: CognitionContext;
  async decide(input: Perception, context: CognitionContext): Promise<Decision> { this.context = context; return { actionId: input.candidates[0].id, intent: "continue", reflection: null, speech: null }; }
}

class ProjectProvider implements CognitionProvider {
  calls=0;
  async decide(input:Perception,context:CognitionContext):Promise<Decision>{this.calls++;if(this.calls===1)return{actionId:input.candidates[0].id,intent:"begin",reflection:null,speech:null,projectUpdate:{operation:"CREATE",title:"Observe the hill",purpose:"I want to understand it.",progress:"I began thinking about it."}};if(this.calls===3)return{actionId:input.candidates[0].id,intent:"finish",reflection:null,speech:null,projectUpdate:{operation:"COMPLETE",projectId:context.projects[0]?.id,progress:"I am done with this for now."}};return{actionId:input.candidates[0].id,intent:"continue",reflection:null,speech:null};}
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

test("self-authored state and reflections remain outside factual episodes", async () => {
  const store = new MemoryStore(await mkdtemp(join(tmpdir(), "ec-context-")));
  await store.writeState(agentId, { goals: ["Understand this place."], intent: "Look around.", updatedAt: "now" });
  await store.appendReflection(agentId, { at: "now", text: "The horizon seems open." });
  await store.appendEpisode(agentId, { at: "now", epoch: 1, kind: "OUTCOME", text: "Waited." });
  const provider = new CapturingProvider(); const server = createBrainServer(provider, store); server.listen(0, "127.0.0.1"); await once(server, "listening");
  try {
    const port = (server.address() as { port: number }).port;
    const response = await fetch(`http://127.0.0.1:${port}/v1/decide`, { method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify(perception) });
    assert.equal(response.status, 200);
    assert.deepEqual(provider.context?.self.goals, ["Understand this place."]);
    assert.ok(provider.context?.working.some(event=>event.text==="Waited."));
    assert.ok(!provider.context?.working.some(event=>event.text==="The horizon seems open."));
  } finally { server.close(); await once(server, "close"); }
});

test("self-authored projects persist across decisions and restart",async()=>{const root=await mkdtemp(join(tmpdir(),"ec-project-")),store=new MemoryStore(root),provider=new ProjectProvider(),server=createBrainServer(provider,store);server.listen(0,"127.0.0.1");await once(server,"listening");try{const url=`http://127.0.0.1:${(server.address() as {port:number}).port}/v1/decide`;for(let epoch=0;epoch<3;epoch++)assert.equal((await fetch(url,{method:"POST",headers:{"content-type":"application/json"},body:JSON.stringify({...perception,epoch})})).status,200);const projects=await new MemoryStore(root).projects(agentId);assert.equal(projects.length,1);assert.equal(projects[0].status,"COMPLETED");}finally{server.close();await once(server,"close");}});
test("a 40-candidate perception reaches the provider through /v1/decide",async()=>{const store=new MemoryStore(await mkdtemp(join(tmpdir(),"ec-40-")));let calls=0;const provider: CognitionProvider={async decide(input){calls++;return {actionId:input.candidates[39].id,intent:"choose final",reflection:null,speech:null};}};const server=createBrainServer(provider,store);server.listen(0,"127.0.0.1");await once(server,"listening");try{const payload={...perception,candidates:Array.from({length:40},(_,index)=>({id:`wait:${index}`,kind:"WAIT",description:`Wait ${index}.`}))};const response=await fetch(`http://127.0.0.1:${(server.address() as {port:number}).port}/v1/decide`,{method:"POST",headers:{"content-type":"application/json"},body:JSON.stringify(payload)});assert.equal(response.status,200);assert.equal(calls,1);assert.equal((await response.json() as {actionId:string}).actionId,"wait:39");}finally{server.close();await once(server,"close");}});
test("low-priority subjective continuity preserves a self-authored project across ordinary decisions",async()=>{const store=new MemoryStore(await mkdtemp(join(tmpdir(),"ec-continuity-")));let continuations=0;const provider:CognitionProvider={async decide(input){return {actionId:input.candidates[0].id,intent:"observe",reflection:null,speech:null};},async continueSubjectively(){continuations++;return {projectUpdate:{operation:"CREATE",title:"A small shelter",purpose:"I want to arrange my carried blocks.",progress:"Considering a first step."}};}};const server=createBrainServer(provider,{model:"andy",profile:"minecraft-andy4"},store);server.listen(0,"127.0.0.1");await once(server,"listening");try{const url=`http://127.0.0.1:${(server.address() as {port:number}).port}/v1/decide`;for(let epoch=0;epoch<6;epoch++)assert.equal((await fetch(url,{method:"POST",headers:{"content-type":"application/json"},body:JSON.stringify({...perception,epoch})})).status,200);let projects=await store.projects(agentId);for(let attempt=0;attempt<20&&projects.length===0;attempt++){await new Promise(resolve=>setTimeout(resolve,10));projects=await store.projects(agentId);}assert.equal(continuations,1);assert.equal(projects[0].title,"A small shelter");}finally{server.close();await once(server,"close");}});
