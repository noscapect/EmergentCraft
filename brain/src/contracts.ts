import { z } from "zod";

const Position = z.object({ x: z.number(), y: z.number(), z: z.number() });
const TargetPosition = Position.extend({ x: z.number().int(), y: z.number().int(), z: z.number().int() });
export const CandidateSchema = z.object({ id: z.string().min(1).max(128).regex(/^[A-Za-z0-9:_-]+$/), kind: z.enum(["WAIT", "MOVE_TO", "APPROACH_ENTITY", "APPROACH_BLOCK", "BREAK_BLOCK", "PICK_UP_ITEM", "SPEAK"]), description: z.string().min(1).max(240), targetId: z.string().max(128).nullish(), target: TargetPosition.nullish() });
export type Candidate = z.infer<typeof CandidateSchema>;
const ObservedEntity = z.object({ id: z.string().max(128), type: z.string().max(100), relativePosition: Position, distance: z.number().nonnegative(), name: z.string().max(100).nullable(), alive: z.boolean(), emergentCraft: z.object({ agentId: z.string().uuid(), name: z.string().max(32) }).nullable(), burning: z.boolean().nullish(), baby: z.boolean().nullish() });
const ObservedBlock = z.object({ id: z.string().max(128), type: z.string().max(100), relativePosition: Position, distance: z.number().nonnegative(), hardness: z.number(), breakable: z.boolean(), solid: z.boolean(), fluid: z.boolean(), position: TargetPosition });
const ObservedItem = z.object({ id: z.string().max(128), item: z.string().max(100), count: z.number().int().positive(), relativePosition: Position, distance: z.number().nonnegative() });
export const PerceptionSchema = z.object({
  schemaVersion: z.literal(1), agentId: z.string().uuid(), name: z.string().min(1).max(32), epoch: z.number().int().nonnegative(),
  capabilities: z.object({ playerHunger: z.boolean(), inventory: z.boolean(), nativeNavigation: z.boolean(), blockBreaking: z.boolean(), itemPickup: z.boolean() }),
  self: z.object({ position: Position, health: z.number().nonnegative(), maxHealth: z.number().positive(), alive: z.boolean(), onGround: z.boolean(), inWater: z.boolean(), onFire: z.boolean(), airSupply: z.number().int(), fallDistance: z.number().nonnegative(), effects: z.array(z.string().max(100)).max(16), inventory: z.array(z.object({ item: z.string().max(100), count: z.number().int().positive() })).max(36) }),
  environment: z.object({ dimension: z.string().max(100), timeOfDay: z.number().int().nonnegative(), weather: z.enum(["clear", "rain", "thunder"]), light: z.number().int().min(0).max(15), biome: z.string().max(100), daytime: z.boolean(), temperature: z.number(), downfall: z.number() }),
  entities: z.array(ObservedEntity).max(16), blocks: z.array(ObservedBlock).max(40), items: z.array(ObservedItem).max(16), events: z.array(z.object({ kind: z.string().max(32), text: z.string().max(240) })).max(16), candidates: z.array(CandidateSchema).min(1).max(32)
});
export type Perception = z.infer<typeof PerceptionSchema>;
export const DecisionSchema = z.object({ actionId: z.string().min(1).max(128), intent: z.string().max(280).default(""), goal: z.string().max(280).nullable().default(null), reflection: z.string().max(400).nullable().default(null), speech: z.string().max(240).nullable().default(null) });
export type Decision = z.infer<typeof DecisionSchema>;
export const EmbodimentEventSchema = z.object({ agentId: z.string().uuid(), epoch: z.number().int().nonnegative(), kind: z.enum(["ACTION", "OUTCOME", "DEATH", "SPEECH", "DAMAGE", "ACQUISITION"]), text: z.string().min(1).max(240) });
export type EmbodimentEvent = z.infer<typeof EmbodimentEventSchema>;
export function validateDecision(input: unknown, perception: Perception): Decision { const decision = DecisionSchema.parse(input); const candidate = perception.candidates.find(value => value.id === decision.actionId); if (!candidate) throw new Error("Decision selected an action outside the offered candidate set"); if (decision.speech && candidate.kind !== "SPEAK") throw new Error("Speech is only accepted with an offered SPEAK action"); return decision; }
