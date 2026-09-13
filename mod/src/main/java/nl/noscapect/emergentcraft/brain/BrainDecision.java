package nl.noscapect.emergentcraft.brain;

/** Public structured cognition only; hidden model reasoning is never transported or stored. */
public record BrainDecision(String actionId,String intent,String goal,String reflection,String speech,BuildPlan buildPlan,SearchRequest search) {
    public record BuildPlan(String purpose,java.util.List<Placement> placements) { }
    public record Placement(int x,int y,int z,String itemId) { }
    public record SearchRequest(String target,int range) { }
}
