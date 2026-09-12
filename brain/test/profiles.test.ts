import test from "node:test";
import assert from "node:assert/strict";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { profileFor, profilePrompt } from "../src/profiles.js";
import { ModelConfigStore } from "../src/model-config.js";

const perception={schemaVersion:1,agentId:"4bde43d4-cbe1-49d0-8a33-9b2e8004881d",name:"Rowan",epoch:2,capabilities:{playerHunger:false,inventory:true,nativeNavigation:true,blockBreaking:true,itemPickup:true},self:{position:{x:0,y:64,z:0},health:20,maxHealth:20,alive:true,onGround:true,inWater:false,onFire:false,airSupply:300,fallDistance:0,effects:[],inventory:[]},environment:{dimension:"minecraft:overworld",timeOfDay:0,weather:"clear",light:10,biome:"minecraft:plains",daytime:true,temperature:0.8,downfall:0},entities:[],blocks:[],items:[],events:[],candidates:[{id:"acquire:oak",kind:"ACQUIRE_BLOCK",target:"minecraft:oak_log",description:"Acquire visible minecraft:oak_log about 5 blocks northwest."}]} as const;
const context={working:[],recalled:[],social:[],knowledge:[],beliefs:[],reflections:[],projects:[],chapters:[],self:{intent:"",updatedAt:new Date(0).toISOString()},contextChars:0};
test("Andy profile compacts only model representation while retaining canonical candidate IDs",()=>{const prompt=profilePrompt(profileFor("minecraft-andy4"),perception,context);assert.equal(prompt.availableActivities[0].id,"acquire:oak");assert.equal(prompt.availableActivities[0].activity,"ACQUIRE_BLOCK");assert.equal(profileFor("minecraft-andy4").sampling.numCtx,16384);});
test("per-agent selections remain isolated operational state",async()=>{const store=new ModelConfigStore(await mkdtemp(join(tmpdir(),"ec-models-")),{provider:"ollama",model:"qwen",profile:"qwen"});const a="4bde43d4-cbe1-49d0-8a33-9b2e8004881d",b="9c354cbb-9405-4dcc-a05e-a79b6fa0bcdf";await store.set(a,{model:"andy",profile:"minecraft-andy4"});assert.equal((await store.get(a)).model,"andy");assert.equal((await store.get(b)).model,"qwen");});
