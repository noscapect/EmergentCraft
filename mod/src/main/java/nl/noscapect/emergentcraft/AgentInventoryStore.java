package nl.noscapect.emergentcraft;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Small, bounded carried inventory keyed by stable agent id, independent of vanilla Villager internals. */
public final class AgentInventoryStore {
    public record Entry(String item,int count) { }
    public record Equipment(String mainHand,String offHand,String head,String chest,String legs,String feet) { public static Equipment empty() { return new Equipment(null,null,null,null,null,null); } }
    private static final int MAX_SLOTS=36, MAX_STACK=64;
    private final Map<UUID,LinkedHashMap<String,Integer>> contents=new HashMap<>();
    private final Map<UUID,Equipment> equipment=new HashMap<>();
    private final Path file=FabricLoader.getInstance().getConfigDir().resolve("emergentcraft/inventory.tsv");
    public AgentInventoryStore() { load(); }
    public List<Entry> entries(UUID id) { return contents.getOrDefault(id,new LinkedHashMap<>()).entrySet().stream().filter(e->e.getValue()>0).limit(MAX_SLOTS).map(e->new Entry(e.getKey(),e.getValue())).toList(); }
    public boolean canAccept(UUID id,String item,int count) { LinkedHashMap<String,Integer> bag=contents.getOrDefault(id,new LinkedHashMap<>()); return (bag.containsKey(item)||bag.size()<MAX_SLOTS)&&count>0&&bag.getOrDefault(item,0)+count<=MAX_STACK; }
    public boolean add(UUID id,String item,int count) { if(!canAccept(id,item,count)) return false; contents.computeIfAbsent(id,ignored->new LinkedHashMap<>()).merge(item,count,Integer::sum); save(); return true; }
    public boolean remove(UUID id,String item,int count) { LinkedHashMap<String,Integer> bag=contents.get(id); if(bag==null||count<=0||bag.getOrDefault(item,0)<count) return false; int left=bag.get(item)-count; if(left==0) bag.remove(item); else bag.put(item,left); save(); return true; }
    public int count(UUID id,String item) { return contents.getOrDefault(id,new LinkedHashMap<>()).getOrDefault(item,0); }
    public Optional<ItemStack> stack(UUID id,String item) { int count=count(id,item); Item resolved=BuiltInRegistries.ITEM.getValue(Identifier.tryParse(item)); return resolved==null||count<=0?Optional.empty():Optional.of(new ItemStack(resolved,Math.min(count,MAX_STACK))); }
    public Equipment equipment(UUID id) { return equipment.getOrDefault(id,Equipment.empty()); }
    public boolean equip(UUID id,String item,String slot) { if(count(id,item)<=0) return false; Equipment old=equipment(id),next=switch(slot) { case "MAINHAND" -> new Equipment(item,old.offHand(),old.head(),old.chest(),old.legs(),old.feet()); case "OFFHAND" -> new Equipment(old.mainHand(),item,old.head(),old.chest(),old.legs(),old.feet()); case "HEAD" -> new Equipment(old.mainHand(),old.offHand(),item,old.chest(),old.legs(),old.feet()); case "CHEST" -> new Equipment(old.mainHand(),old.offHand(),old.head(),item,old.legs(),old.feet()); case "LEGS" -> new Equipment(old.mainHand(),old.offHand(),old.head(),old.chest(),item,old.feet()); case "FEET" -> new Equipment(old.mainHand(),old.offHand(),old.head(),old.chest(),old.legs(),item); default -> null; }; if(next==null) return false; equipment.put(id,next); save(); return true; }
    private void load() { try { if(!Files.exists(file)) return; for(String line:Files.readAllLines(file)) { String[] p=line.split("\\t",-1); if(p.length==3) contents.computeIfAbsent(UUID.fromString(p[0]),ignored->new LinkedHashMap<>()).put(p[1],Integer.parseInt(p[2])); else if(p.length==8&&p[1].equals("equipment")) equipment.put(UUID.fromString(p[0]),new Equipment(empty(p[2]),empty(p[3]),empty(p[4]),empty(p[5]),empty(p[6]),empty(p[7]))); } } catch(Exception e) { EmergentCraftMod.LOGGER.error("Cannot read agent inventory",e); } }
    private static String empty(String value) { return value.isEmpty()?null:value; }
    private void save() { try { Files.createDirectories(file.getParent()); List<String> lines=new ArrayList<>(); for(var bag:contents.entrySet()) for(var stack:bag.getValue().entrySet()) lines.add(bag.getKey()+"\t"+stack.getKey()+"\t"+stack.getValue()); for(var entry:equipment.entrySet()) { Equipment value=entry.getValue(); lines.add(entry.getKey()+"\t"+"equipment\t"+valueOrEmpty(value.mainHand())+"\t"+valueOrEmpty(value.offHand())+"\t"+valueOrEmpty(value.head())+"\t"+valueOrEmpty(value.chest())+"\t"+valueOrEmpty(value.legs())+"\t"+valueOrEmpty(value.feet())); } Files.write(file,lines); } catch(IOException e) { throw new IllegalStateException("Cannot persist agent inventory",e); } }
    private static String valueOrEmpty(String value) { return value==null?"":value; }
}
