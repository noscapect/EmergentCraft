package nl.noscapect.emergentcraft;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Small, bounded carried inventory keyed by stable agent id, independent of vanilla Villager internals. */
public final class AgentInventoryStore {
    public record Entry(String item,int count) { }
    private static final int MAX_SLOTS=36, MAX_STACK=64;
    private final Map<UUID,LinkedHashMap<String,Integer>> contents=new HashMap<>();
    private final Path file=FabricLoader.getInstance().getConfigDir().resolve("emergentcraft/inventory.tsv");
    public AgentInventoryStore() { load(); }
    public List<Entry> entries(UUID id) { return contents.getOrDefault(id,new LinkedHashMap<>()).entrySet().stream().filter(e->e.getValue()>0).limit(MAX_SLOTS).map(e->new Entry(e.getKey(),e.getValue())).toList(); }
    public boolean canAccept(UUID id,String item,int count) { LinkedHashMap<String,Integer> bag=contents.getOrDefault(id,new LinkedHashMap<>()); return (bag.containsKey(item)||bag.size()<MAX_SLOTS)&&count>0&&bag.getOrDefault(item,0)+count<=MAX_STACK; }
    public boolean add(UUID id,String item,int count) { if(!canAccept(id,item,count)) return false; contents.computeIfAbsent(id,ignored->new LinkedHashMap<>()).merge(item,count,Integer::sum); save(); return true; }
    private void load() { try { if(!Files.exists(file)) return; for(String line:Files.readAllLines(file)) { String[] p=line.split("\\t",-1); if(p.length==3) contents.computeIfAbsent(UUID.fromString(p[0]),ignored->new LinkedHashMap<>()).put(p[1],Integer.parseInt(p[2])); } } catch(Exception e) { EmergentCraftMod.LOGGER.error("Cannot read agent inventory",e); } }
    private void save() { try { Files.createDirectories(file.getParent()); List<String> lines=new ArrayList<>(); for(var bag:contents.entrySet()) for(var stack:bag.getValue().entrySet()) lines.add(bag.getKey()+"\t"+stack.getKey()+"\t"+stack.getValue()); Files.write(file,lines); } catch(IOException e) { throw new IllegalStateException("Cannot persist agent inventory",e); } }
}
