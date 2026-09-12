package nl.noscapect.emergentcraft;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Persistent body physics. It exposes hunger but never decides whether an inhabitant should eat. */
public final class AgentMetabolismStore {
    public record State(int hunger,float saturation,long lastTick) { public boolean starving() { return hunger<=0; } }
    private static final int MAX_HUNGER=20;
    private final Map<UUID,State> states=new HashMap<>();
    private final Path file=FabricLoader.getInstance().getConfigDir().resolve("emergentcraft/metabolism.tsv");
    public AgentMetabolismStore() { load(); }
    public State state(UUID agent,long tick) { return states.computeIfAbsent(agent,ignored->new State(MAX_HUNGER,5f,tick)); }
    /** Drains at most once per Minecraft day at rest; active skills add bounded exertion. */
    public State tick(UUID agent,long tick,boolean exerting) { State old=state(agent,tick); long interval=exerting?6000:12000; if(tick-old.lastTick()<interval) return old; int steps=(int)Math.min(4,(tick-old.lastTick())/interval); State next=drained(old,steps,tick); states.put(agent,next); save(); return next; }
    public State eat(UUID agent,long tick,ItemStack stack) { FoodProperties food=stack.get(DataComponents.FOOD); if(food==null) return state(agent,tick); State old=state(agent,tick); int hunger=Math.min(MAX_HUNGER,old.hunger()+food.nutrition()); float saturation=Math.min(hunger,old.saturation()+food.saturation()); State next=new State(hunger,saturation,tick); states.put(agent,next); save(); return next; }
    public State drain(UUID agent,int amount,long tick) { State next=drained(state(agent,tick),amount,tick); states.put(agent,next); save(); return next; }
    public static State drained(State old,int amount,long tick) { float saturation=Math.max(0,old.saturation()-amount); int remaining=(int)Math.max(0,amount-(old.saturation()-saturation)); return new State(Math.max(0,old.hunger()-remaining),saturation,tick); }
    private void load() { try { if(!Files.exists(file)) return; for(String line:Files.readAllLines(file)) { String[] parts=line.split("\\t",-1); if(parts.length==4) states.put(UUID.fromString(parts[0]),new State(Integer.parseInt(parts[1]),Float.parseFloat(parts[2]),Long.parseLong(parts[3]))); } } catch(Exception error) { EmergentCraftMod.LOGGER.error("Cannot read agent metabolism",error); } }
    private void save() { try { Files.createDirectories(file.getParent()); List<String> lines=new ArrayList<>(); for(var entry:states.entrySet()) { State state=entry.getValue(); lines.add(entry.getKey()+"\\t"+state.hunger()+"\\t"+state.saturation()+"\\t"+state.lastTick()); } Files.write(file,lines); } catch(IOException error) { throw new IllegalStateException("Cannot persist agent metabolism",error); } }
}
