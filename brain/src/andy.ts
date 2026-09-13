import type { Candidate, Decision, Perception } from "./contracts.js";

/**
 * Safe, small command-language adapter for the Andy/Mindcraft model family.
 *
 * Adapted concepts (not executable code) from Mindcraft's MIT command parser
 * and Ollama cleanup at 5f3acc87b479864124173de444f31fa5538f94a6.  Commands
 * are model language only: every result is resolved back to a current offered
 * candidate before it can become a Decision.
 */
export type AndyCommand =
  | { name:"collectBlocks"; args:[string,number] }
  | { name:"collectDrops"; args:[string,number] }
  | { name:"consume"|"equip"|"craftRecipe"|"placeHere"|"attack"; args:[string] | [string,number] }
  | { name:"goToPlayer"; args:[string,number] }
  | { name:"goToCoordinates"; args:[number,number,number,number] }
  | { name:"stay"; args:[number] }
  | { name:"newAction"; args:[string] }
  | { name:"stats"|"inventory"|"nearbyBlocks"|"nearbyEntities"|"craftable"; args:[] }
  | { name:"searchForEntity"|"searchForBlock"; args:[string,number] };

type Definition={count:number; kinds:("string"|"number"|"integer")[]};
const definitions:Record<AndyCommand["name"],Definition>={
  collectBlocks:{count:2,kinds:["string","integer"]}, collectDrops:{count:2,kinds:["string","integer"]},
  consume:{count:1,kinds:["string"]},equip:{count:1,kinds:["string"]},craftRecipe:{count:2,kinds:["string","integer"]},placeHere:{count:1,kinds:["string"]},attack:{count:1,kinds:["string"]},
  goToPlayer:{count:2,kinds:["string","number"]},goToCoordinates:{count:4,kinds:["number","number","number","number"]},stay:{count:1,kinds:["number"]},newAction:{count:1,kinds:["string"]},
  stats:{count:0,kinds:[]},inventory:{count:0,kinds:[]},nearbyBlocks:{count:0,kinds:[]},nearbyEntities:{count:0,kinds:[]},craftable:{count:0,kinds:[]},searchForEntity:{count:2,kinds:["string","number"]},searchForBlock:{count:2,kinds:["string","number"]}
};
export type CommandParse={ok:true;command:AndyCommand;commandText:string}|{ok:false;error:string};
export type CleanedAndyOutput={text:string;incompleteThink:boolean};

/** Mindcraft retries an unclosed private-reasoning block.  Never expose it. */
export function cleanAndyOutput(raw:string):CleanedAndyOutput {
  let text=raw.replace(/\r/g,"");
  const opened=text.includes("<think>"),closed=text.includes("</think>");
  if(opened&&!closed)return {text:"",incompleteThink:true};
  if(closed&&!opened)text="<think>"+text;
  text=text.replace(/<think>[\s\S]*?<\/think>/g,"");
  // Code is neither a public intention nor an executable capability here.
  text=text.replace(/```[\s\S]*?```/g,"");
  return {text:text.trim().slice(0,1600),incompleteThink:false};
}

function parseArguments(source:string):string[]|null {
  if(!source.trim())return [];
  const result:string[]=[];let current="",quote="",escaped=false;
  for(const char of source){
    if(escaped){current+=char;escaped=false;continue;}
    if(char==="\\"&&quote){escaped=true;continue;}
    if((char==='"'||char==="'")&&(!quote||quote===char)){quote=quote?"":char;continue;}
    if(char===","&&!quote){result.push(current.trim());current="";continue;}
    current+=char;
  }
  if(quote||escaped)return null;result.push(current.trim());return result;
}

/** Parses one Mindcraft-style command; unknown commands and loose syntax fail closed. */
export function parseAndyCommand(text:string):CommandParse {
  const match=/!([A-Za-z][A-Za-z0-9_]*)\(([^()]*)\)/.exec(text);
  if(!match)return {ok:false,error:"No well-formed command."};
  const name=match[1] as AndyCommand["name"],definition=definitions[name];
  if(!definition)return {ok:false,error:`Unknown command: !${match[1]}.`};
  const raw=parseArguments(match[2]);if(!raw)return {ok:false,error:"Malformed command arguments."};
  if(raw.length!==definition.count)return {ok:false,error:`!${name} requires ${definition.count} argument(s).`};
  const args:(string|number)[]=[];
  for(let index=0;index<raw.length;index++){
    const expected=definition.kinds[index],value=raw[index];
    if(expected==="string"){
      if(!value||/[`{};]/.test(value))return {ok:false,error:`Invalid string argument ${index+1}.`};
      args.push(value.trim());
    }else { const number=Number(value);if(!Number.isFinite(number)||(expected==="integer"&&!Number.isInteger(number)))return {ok:false,error:`Argument ${index+1} must be ${expected}.`};if((name==="collectBlocks"||name==="collectDrops"||name==="craftRecipe")&&number<1)return {ok:false,error:"Count must be positive."};if((name==="goToPlayer"||name==="goToCoordinates"||name==="stay"||name==="searchForEntity"||name==="searchForBlock")&&number<0)return {ok:false,error:"Distance/closeness must be zero or greater."};args.push(number); }
  }
  return {ok:true,command:{name,args:args as never},commandText:match[0]};
}

const key=(value:string)=>value.toLowerCase().replace(/^minecraft:/,"").replace(/[ _-]/g,"");
const stable=<T extends Candidate>(values:T[])=>values.slice().sort((a,b)=>a.id.localeCompare(b.id));
const first=(values:Candidate[])=>stable(values)[0];
const typeFor=(candidate:Candidate,perception:Perception)=>{
  if(candidate.kind==="ACQUIRE_BLOCK"||candidate.kind==="APPROACH_BLOCK")return perception.blocks.find(block=>block.id===candidate.targetId)?.type;
  if(candidate.kind==="PICK_UP_ITEM")return perception.items.find(item=>item.id===candidate.targetId)?.item;
  if(candidate.kind==="ENGAGE"||candidate.kind==="APPROACH_ENTITY")return perception.entities.find(entity=>entity.id===candidate.targetId)?.type;
  return candidate.targetId??candidate.description;
};
const nearest=(values:Candidate[],perception:Perception,kind:"block"|"item"|"entity")=>values.slice().sort((a,b)=>{
  const distance=(candidate:Candidate)=>kind==="block"?perception.blocks.find(value=>value.id===candidate.targetId)?.distance??Infinity:kind==="item"?perception.items.find(value=>value.id===candidate.targetId)?.distance??Infinity:perception.entities.find(value=>value.id===candidate.targetId)?.distance??Infinity;
  return distance(a)-distance(b)||a.id.localeCompare(b.id);
})[0];

export type CommandMatch={candidate:Candidate;command:AndyCommand}|{error:string};
export const isAndyInfoCommand=(command:AndyCommand)=>["stats","inventory","nearbyBlocks","nearbyEntities","craftable"].includes(command.name);
const compact=(values:string[])=>values.slice(0,12).join(", ")||"none";
/** Read-only compatibility tools summarize this exact snapshot; they never query the world. */
export function andyInfoResult(command:AndyCommand,perception:Perception):string {
  switch(command.name){
    case "stats": return `health ${perception.self.health}/${perception.self.maxHealth}; hunger ${perception.self.hunger?`${perception.self.hunger.current}/${perception.self.hunger.max}`:"unavailable"}; equipment ${perception.self.equipment?Object.entries(perception.self.equipment).filter(([,v])=>v).map(([k,v])=>`${k}:${v}`).join(", ")||"none":"unavailable"}; ${perception.environment.dimension}, ${perception.environment.weather}, light ${perception.environment.light}.`;
    case "inventory": return `inventory: ${compact(perception.self.inventory.map(entry=>`${entry.item.replace(/^minecraft:/,"")} x${entry.count}`))}.`;
    case "nearbyBlocks": { const counts=new Map<string,number>(); for(const block of perception.blocks)counts.set(block.type.replace(/^minecraft:/,""),(counts.get(block.type.replace(/^minecraft:/,""))??0)+1); return `visible blocks: ${compact([...counts].sort(([a],[b])=>a.localeCompare(b)).map(([type,count])=>`${type} x${count}`))}.`; }
    case "nearbyEntities": return `perceived entities: ${compact(perception.entities.slice().sort((a,b)=>a.distance-b.distance||a.id.localeCompare(b.id)).map(entity=>`${entity.name??entity.type.replace(/^minecraft:/,"")} (${Math.round(entity.distance*10)/10})`))}.`;
    case "craftable": return `offered craft recipes: ${compact(perception.candidates.filter(candidate=>candidate.kind==="CRAFT").map(candidate=>(candidate.targetId??candidate.description).replace(/^minecraft:/,"" )).sort())}.`;
    default: return "Not an informational command.";
  }
}
/** The only command-to-world bridge.  It can select, never create, an affordance. */
export function matchAndyCommand(command:AndyCommand,perception:Perception):CommandMatch {
  const candidates=perception.candidates;
  const byType=(kind:Candidate["kind"],wanted:string,source:"block"|"item"|"entity")=>nearest(candidates.filter(candidate=>candidate.kind===kind&&key(typeFor(candidate,perception)??"")===key(wanted)),perception,source);
  let candidate:Candidate|undefined;
  switch(command.name){
    case "collectBlocks":candidate=byType("ACQUIRE_BLOCK",command.args[0],"block");break;
    case "collectDrops":candidate=byType("PICK_UP_ITEM",command.args[0],"item");break;
    case "consume":candidate=first(candidates.filter(value=>value.kind==="EAT_ITEM"&&key(value.targetId??"")===key(command.args[0] as string)));break;
    case "equip":candidate=first(candidates.filter(value=>value.kind==="EQUIP_ITEM"&&key(value.targetId??"")===key(command.args[0] as string)));break;
    case "craftRecipe":candidate=first(candidates.filter(value=>value.kind==="CRAFT"&&key(value.targetId??value.description)===key(command.args[0] as string)));break;
    case "placeHere":candidate=nearest(candidates.filter(value=>value.kind==="PLACE_BLOCK"&&key(value.targetId??"")===key(command.args[0] as string)),perception,"block");break;
    case "attack":candidate=byType("ENGAGE",command.args[0] as string,"entity");break;
    case "goToPlayer": { const name=key(command.args[0] as string);candidate=first(candidates.filter(value=>{const entity=perception.entities.find(entity=>entity.id===value.targetId);return value.kind==="APPROACH_ENTITY"&&key(entity?.name??"")===name&&(entity?.type==="minecraft:player"||entity?.emergentCraft!=null);}));break; }
    case "goToCoordinates": { const [x,y,z]=command.args;candidate=first(candidates.filter(value=>value.kind==="TRAVEL"&&value.target?.x===x&&value.target?.y===y&&value.target?.z===z));break; }
    case "stay":candidate=first(candidates.filter(value=>value.kind==="WAIT"));break;
    case "newAction": { if(/(?:=>|function\s*\(|\b(?:javascript|code|import|await)\b|[;`{}])/i.test(command.args[0]))return {error:"newAction contains code-like text."};candidate=first(candidates.filter(value=>value.kind==="BUILD_PLAN"));break; }
  }
  if(command.name==="searchForEntity"){candidate=byType("APPROACH_ENTITY",command.args[0],"entity");return candidate?{candidate,command}:{error:`No currently perceived/offered ${command.args[0]} is available. Long-range search is not available in this body.`};}
  if(command.name==="searchForBlock"){candidate=nearest(candidates.filter(value=>(value.kind==="APPROACH_BLOCK"||value.kind==="ACQUIRE_BLOCK")&&key(typeFor(value,perception)??"")===key(command.args[0])),perception,"block");return candidate?{candidate,command}:{error:`No currently perceived/offered ${command.args[0]} is available. Long-range search is not available in this body.`};}
  if(command.name==="goToCoordinates"){const [x,y,z]=command.args;candidate=candidates.filter(value=>value.kind==="TRAVEL"&&value.target).map(value=>({value,distance:Math.hypot(value.target!.x-x,value.target!.y-y,value.target!.z-z)})).filter(value=>value.distance<=1.5).sort((a,b)=>a.distance-b.distance||a.value.id.localeCompare(b.value.id))[0]?.value;}
  if(candidate)return {candidate,command};
  if(command.name==="craftRecipe")return {error:`No offered craft recipe produces ${command.args[0]}.`};
  if(command.name==="newAction")return {error:"Custom/build activity is not currently available with your present inventory and affordances."};
  if(command.name==="attack")return {error:`No currently visible/offered ${command.args[0]} can be attacked.`};
  return {error:`No currently offered candidate matches !${command.name}.`};
}

const artifact=/(<\/?think>|```|\bjavascript\b|the user has just told me|system prompt|json schema|\bactionid\b|^\s*\/\/|\b(?:const|let|var|function)\b|=>)/i;
/** Public text is optional.  Prompt/code debris must not become subjective memory. */
export function safePublicIntent(text:string,commandText:string):string {
  const publicText=text.replace(commandText,"").trim().replace(/\s+/g," ");
  if(!publicText||artifact.test(publicText))return "";
  return publicText.slice(0,280);
}

/** Dynamic, semantic command view: one line per available action family, not 40 opaque indexes. */
export function andyCommandDocs(perception:Perception):string {
  const lines:string[]=["!stats() !inventory() !nearbyBlocks() !nearbyEntities() !craftable() — inspect only current observations."]; const add=(line:string)=>{if(!lines.includes(line))lines.push(line);};
  for(const candidate of perception.candidates){
    const type=typeFor(candidate,perception);
    if(candidate.kind==="ACQUIRE_BLOCK"&&type)add(`!collectBlocks(\"${type.replace(/^minecraft:/,"")}\", 1) — collect a visible ${type}.`);
    if(candidate.kind==="PICK_UP_ITEM"&&type)add(`!collectDrops(\"${type.replace(/^minecraft:/,"")}\", 1) — pick up a visible ${type}.`);
    if(candidate.kind==="EAT_ITEM"&&candidate.targetId)add(`!consume(\"${candidate.targetId.replace(/^minecraft:/,"")}\") — eat carried ${candidate.targetId}.`);
    if(candidate.kind==="EQUIP_ITEM"&&candidate.targetId)add(`!equip(\"${candidate.targetId.replace(/^minecraft:/,"")}\") — equip carried ${candidate.targetId}.`);
    if(candidate.kind==="CRAFT"&&candidate.targetId)add(`!craftRecipe(\"${candidate.targetId.replace(/^minecraft:/,"")}\", 1) — craft that offered recipe.`);
    if(candidate.kind==="PLACE_BLOCK"&&candidate.targetId)add(`!placeHere(\"${candidate.targetId.replace(/^minecraft:/,"")}\") — place a carried block at an offered nearby position.`);
    if(candidate.kind==="ENGAGE"&&type)add(`!attack(\"${type.replace(/^minecraft:/,"")}\") — engage a visible ${type}.`);
    if(candidate.kind==="APPROACH_ENTITY"){const entity=perception.entities.find(value=>value.id===candidate.targetId);if(entity?.name&&(entity.type==="minecraft:player"||entity.emergentCraft!=null))add(`!goToPlayer(\"${entity.name}\", 3) — approach ${entity.name}.`);if(entity) add(`!searchForEntity(\"${entity.type.replace(/^minecraft:/,"")}\", 16) — approach this perceived entity only.`);}
    if((candidate.kind==="APPROACH_BLOCK"||candidate.kind==="ACQUIRE_BLOCK")&&type)add(`!searchForBlock(\"${type.replace(/^minecraft:/,"")}\", 16) — use this visible block only.`);
    if(candidate.kind==="BUILD_PLAN")add(`!newAction(\"Describe a small build using carried blocks\") — request a bounded build plan.`);
  }
  const travel=perception.candidates.filter(value=>value.kind==="TRAVEL"&&value.target).slice(0,3);for(const candidate of travel)add(`!goToCoordinates(${candidate.target!.x}, ${candidate.target!.y}, ${candidate.target!.z}, 2) — travel to this already offered destination.`);
  if(perception.candidates.some(value=>value.kind==="WAIT"))add("!stay(5) — wait and observe.");
  return lines.length?lines.join("\n"):"No physical command is currently available.";
}

export const commandIntent=(command:AndyCommand)=>`!${command.name}`;
export type AndyDiagnostic={rawCleaned:string;command:string|null;candidateId:string|null;valid:boolean;error?:string};
export function toAndyDecision(raw:string,perception:Perception):{decision:Decision;diagnostic:AndyDiagnostic;command:AndyCommand}|{error:string;diagnostic:AndyDiagnostic}{
  const cleaned=cleanAndyOutput(raw);if(cleaned.incompleteThink)return {error:"Andy returned an incomplete private-thought block.",diagnostic:{rawCleaned:"",command:null,candidateId:null,valid:false}};
  const parsed=parseAndyCommand(cleaned.text);if(!parsed.ok)return {error:parsed.error,diagnostic:{rawCleaned:cleaned.text,command:null,candidateId:null,valid:false,error:parsed.error}};
  const matched=matchAndyCommand(parsed.command,perception);if("error" in matched)return {error:matched.error,diagnostic:{rawCleaned:cleaned.text,command:parsed.commandText,candidateId:null,valid:false,error:matched.error}};
  const intent=safePublicIntent(cleaned.text,parsed.commandText)||commandIntent(parsed.command);
  return {decision:{actionId:matched.candidate.id,intent,reflection:null,speech:null},command:parsed.command,diagnostic:{rawCleaned:cleaned.text,command:parsed.commandText,candidateId:matched.candidate.id,valid:true}};
}
