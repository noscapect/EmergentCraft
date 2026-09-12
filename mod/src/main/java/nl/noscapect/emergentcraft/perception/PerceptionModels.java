package nl.noscapect.emergentcraft.perception;

import java.util.List;

/** JSON contract types shared with brain/src/contracts.ts. Minecraft values enter here, not prompts. */
public final class PerceptionModels {
    private PerceptionModels() { }
    public record Position(double x,double y,double z) { }
    public record InventoryEntry(String item,int count) { }
    public record Hunger(int current,int max,float saturation,boolean starving) { }
    public record Equipment(String mainHand,String offHand,String head,String chest,String legs,String feet) { }
    public record Capabilities(boolean playerHunger,boolean inventory,boolean nativeNavigation,boolean blockBreaking,boolean itemPickup,boolean eating,boolean equipment,boolean combat,boolean crafting,boolean placement) { }
    public record SelfPerception(Position position,float health,float maxHealth,boolean alive,boolean onGround,boolean inWater,boolean onFire,int airSupply,float fallDistance,List<String> effects,List<InventoryEntry> inventory,Hunger hunger,Equipment equipment) { }
    public record EnvironmentPerception(String dimension,long timeOfDay,String weather,int light,String biome,boolean daytime,float temperature,float downfall) { }
    public record EmergentIdentity(String agentId,String name) { }
    public record ObservedEntity(String id,String type,Position relativePosition,double distance,String name,boolean alive,EmergentIdentity emergentCraft,Boolean burning,Boolean baby) { }
    public record ObservedBlock(String id,String type,Position relativePosition,double distance,float hardness,boolean breakable,boolean solid,boolean fluid,BlockPosition position) { }
    public record ObservedItem(String id,String item,int count,Position relativePosition,double distance) { }
    public record BlockPosition(int x,int y,int z) { }
    public record RecentEvent(String kind,String text) { }
    public record ActionCandidate(String id,String kind,String description,String targetId,BlockPosition target) { }
    public record AgentPerception(int schemaVersion,String agentId,String name,long epoch,Capabilities capabilities,SelfPerception self,EnvironmentPerception environment,List<ObservedEntity> entities,List<ObservedBlock> blocks,List<ObservedItem> items,List<RecentEvent> events,List<ActionCandidate> candidates) { }
}
