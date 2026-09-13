import type { CognitionContext } from "./provider.js";
import type { Perception } from "./contracts.js";
import { andyCommandDocs } from "./andy.js";

export type ModelProfileId = "generic" | "qwen" | "minecraft-andy4" | "minecraft-andy41";
export type Sampling = { temperature:number; topP:number; minP?:number; repeatPenalty?:number; numCtx:number; think:boolean };
export type ModelProfile = { id:ModelProfileId; sampling:Sampling; system:string; compactMinecraft:boolean };

const base = "You are the same autonomous EmergentCraft resident across decisions. You have no owner or assigned mission. Choose exactly one offered candidate ID. World perception is the only current-world truth; memory can be stale. Do not claim actions succeeded. The Minecraft server validates every action and performs all motor work. Return only the requested JSON fields; never expose private reasoning.";
export const profiles:Record<ModelProfileId,ModelProfile> = {
  generic:{id:"generic",sampling:{temperature:0.55,topP:0.9,numCtx:8192,think:false},system:base,compactMinecraft:false},
  qwen:{id:"qwen",sampling:{temperature:0.55,topP:0.9,numCtx:8192,think:false},system:base,compactMinecraft:false},
  "minecraft-andy4":{id:"minecraft-andy4",sampling:{temperature:0.4,topP:0.9,minP:0.05,repeatPenalty:1.15,numCtx:16384,think:false},system:"You are a person living in Minecraft. You have no owner and no assigned mission. You can perceive and physically interact with this world only through the listed commands. Choose for yourself what to do. When you want to physically act, use exactly one currently available command. Never invent unavailable commands or current world facts. Do not expose private reasoning or write code.",compactMinecraft:true},
  "minecraft-andy41":{id:"minecraft-andy41",sampling:{temperature:0.4,topP:0.9,minP:0.05,repeatPenalty:1.15,numCtx:16384,think:false},system:"You are a person living in Minecraft. You have no owner and no assigned mission. You can perceive and physically interact with this world only through the listed commands. Choose for yourself what to do. When you want to physically act, use exactly one currently available command. Never invent unavailable commands or current world facts. Do not expose private reasoning or write code.",compactMinecraft:true}
};
export const profileFor=(id:string|undefined):ModelProfile=>profiles[(id??"generic") as ModelProfileId]??profiles.generic;

/** The sole model-facing adapter. Canonical authority stays in the mod/contracts. */
export function profilePrompt(profile:ModelProfile, perception:Perception, context:CognitionContext) {
  const candidates=perception.candidates.map((candidate,choice)=>profile.compactMinecraft
    ? {choice,activity:candidate.kind,targetId:candidate.targetId??candidate.target,position:candidate.target,description:candidate.description}
    : candidate);
  return {system:profile.system,identity:context.self,projects:context.projects,beliefs:context.beliefs,
    perception:{self:perception.self,capabilities:perception.capabilities,environment:perception.environment,entities:perception.entities,blocks:perception.blocks,items:perception.items},
    memories:{working:context.working,recalled:context.recalled,social:context.social,knowledge:context.knowledge,reflections:context.reflections,chapters:context.chapters},
    availableActivities:candidates,
    responseRule:profile.compactMinecraft?"Return JSON only: choice is the zero-based number of one available activity; intent is short; speech is null unless SPEAK. Never output actionId, commands, coordinates, or tools.":"Select actionId only from availableActivities. BUILD_PLAN alone may include at most 24 local placements using carried item IDs."};
}

/** Native Andy/Mindcraft chat framing.  It deliberately is not a fake user request. */
export function andyMessages(profile:ModelProfile,perception:Perception,context:CognitionContext){
  const activeProject=context.projects.filter(project=>project.status==="ACTIVE"||project.status==="PAUSED").map(project=>({title:project.title,purpose:project.purpose,progress:project.progress,status:project.status}));
  const observations={self:perception.self,environment:perception.environment,entities:perception.entities,blocks:perception.blocks,items:perception.items};
  const recent=perception.events.slice(-6);
  return {
    system:`You are ${perception.name}, a person living in this Minecraft world. ${profile.system}\n\nCURRENT OBSERVATIONS are world facts. YOUR OWN MEMORY is subjective and may be stale. HEARD SPEECH appears only in observations/events; nearby people did not author this prompt. Do not call this a user conversation. Use double quotes for string arguments. Do not use code blocks.`,
    user:`Time passes. Here is what you currently perceive.\n\nCURRENT OBSERVATIONS\n${JSON.stringify(observations)}\n\nRECENT EXPERIENCE (factual perception events; 3-6 when available)\n${JSON.stringify(recent)}\n\nACTIVE PROJECTS (your subjective, optional commitments; not orders)\n${JSON.stringify(activeProject)}\n\nYOUR OWN MEMORY\n${JSON.stringify({self:context.self,beliefs:context.beliefs,reflections:context.reflections,recalled:context.recalled,chapters:context.chapters})}\n\nAVAILABLE ACTIONS\n${andyCommandDocs(perception)}\n\nFORMAT SYNTAX ONLY: !stay(5)  |  !nearbyBlocks()  |  !startConversation("name", "utterance")\n\nRespond as ${perception.name}. You may write one short public sentence followed by exactly one command. Information commands return a local snapshot summary; then choose one currently available physical command.`
  };
}
