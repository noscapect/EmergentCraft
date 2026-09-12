import type { CognitionContext } from "./provider.js";
import type { Perception } from "./contracts.js";

export type ModelProfileId = "generic" | "qwen" | "minecraft-andy4" | "minecraft-andy41";
export type Sampling = { temperature:number; topP:number; minP?:number; repeatPenalty?:number; numCtx:number; think:boolean };
export type ModelProfile = { id:ModelProfileId; sampling:Sampling; system:string; compactMinecraft:boolean };

const base = "You are the same autonomous EmergentCraft resident across decisions. You have no owner or assigned mission. Choose exactly one offered candidate ID. World perception is the only current-world truth; memory can be stale. Do not claim actions succeeded. The Minecraft server validates every action and performs all motor work. Return only the requested JSON fields; never expose private reasoning.";
export const profiles:Record<ModelProfileId,ModelProfile> = {
  generic:{id:"generic",sampling:{temperature:0.55,topP:0.9,numCtx:8192,think:false},system:base,compactMinecraft:false},
  qwen:{id:"qwen",sampling:{temperature:0.55,topP:0.9,numCtx:8192,think:false},system:base,compactMinecraft:false},
  "minecraft-andy4":{id:"minecraft-andy4",sampling:{temperature:0.4,topP:0.9,minP:0.05,repeatPenalty:1.15,numCtx:16384,think:false},system:base+" You understand Minecraft mechanics, but may use only candidates offered by this body.",compactMinecraft:true},
  "minecraft-andy41":{id:"minecraft-andy41",sampling:{temperature:0.4,topP:0.9,minP:0.05,repeatPenalty:1.15,numCtx:16384,think:false},system:base+" Use concise Minecraft-native activity descriptions; never invent commands, coordinates, items, or targets.",compactMinecraft:true}
};
export const profileFor=(id:string|undefined):ModelProfile=>profiles[(id??"generic") as ModelProfileId]??profiles.generic;

/** The sole model-facing adapter. Canonical authority stays in the mod/contracts. */
export function profilePrompt(profile:ModelProfile, perception:Perception, context:CognitionContext) {
  const candidates=perception.candidates.map(candidate=>profile.compactMinecraft
    ? {id:candidate.id,activity:candidate.kind,target:candidate.target,description:candidate.description}
    : candidate);
  return {system:profile.system,identity:context.self,projects:context.projects,beliefs:context.beliefs,
    perception:{self:perception.self,capabilities:perception.capabilities,environment:perception.environment,entities:perception.entities,blocks:perception.blocks,items:perception.items},
    memories:{working:context.working,recalled:context.recalled,social:context.social,knowledge:context.knowledge,reflections:context.reflections,chapters:context.chapters},
    availableActivities:candidates,
    responseRule:"Select actionId only from availableActivities. BUILD_PLAN alone may include at most 24 local placements using carried item IDs."};
}
