package nl.noscapect.emergentcraft.brain;

/** Public structured cognition only; hidden model reasoning is never transported or stored. */
public record BrainDecision(String actionId,String intent,String goal,String reflection,String speech) { }
