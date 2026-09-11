import { z } from "zod";

export const CandidateSchema = z.object({
  id: z.string().min(1).max(128).regex(/^[A-Za-z0-9:_-]+$/),
  kind: z.enum(["WAIT", "MOVE_TO", "SPEAK"]),
  description: z.string().min(1).max(240),
  target: z.object({ x: z.number(), y: z.number(), z: z.number() }).optional()
});
export type Candidate = z.infer<typeof CandidateSchema>;

export const PerceptionSchema = z.object({
  agentId: z.string().uuid(), name: z.string().min(1).max(32), epoch: z.number().int().nonnegative(),
  self: z.object({ position: z.object({ x: z.number(), y: z.number(), z: z.number() }), health: z.number().nonnegative(), hunger: z.number().int().min(0).max(20), heldItem: z.string().max(100) }),
  environment: z.object({ dimension: z.string().max(100), timeOfDay: z.number().int().nonnegative(), weather: z.string().max(30), light: z.number().int().min(0).max(15), biome: z.string().max(100) }),
  entities: z.array(z.object({ type: z.string().max(100), distance: z.number().nonnegative(), direction: z.string().max(20) })).max(16),
  events: z.array(z.string().max(240)).max(16), candidates: z.array(CandidateSchema).min(1).max(24)
});
export type Perception = z.infer<typeof PerceptionSchema>;

export const DecisionSchema = z.object({
  actionId: z.string().min(1).max(128), intent: z.string().max(280).default(""),
  goal: z.string().max(280).nullable().default(null), reflection: z.string().max(400).nullable().default(null),
  speech: z.string().max(240).nullable().default(null)
});
export type Decision = z.infer<typeof DecisionSchema>;

/** Factual events can only be appended by the Minecraft embodiment service. */
export const EmbodimentEventSchema = z.object({
  agentId: z.string().uuid(), epoch: z.number().int().nonnegative(),
  kind: z.enum(["OBSERVATION", "ACTION", "OUTCOME", "DEATH", "SPEECH"]),
  text: z.string().min(1).max(240)
});
export type EmbodimentEvent = z.infer<typeof EmbodimentEventSchema>;

export function validateDecision(input: unknown, perception: Perception): Decision {
  const decision = DecisionSchema.parse(input);
  if (!perception.candidates.some(candidate => candidate.id === decision.actionId)) throw new Error("Decision selected an action outside the offered candidate set");
  return decision;
}
