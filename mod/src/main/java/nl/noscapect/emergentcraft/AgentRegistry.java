package nl.noscapect.emergentcraft;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/** Small line-oriented registry stored outside world save internals. */
public final class AgentRegistry {
    private final Map<String, AgentRecord> byName = new LinkedHashMap<>();
    private final Path file = FabricLoader.getInstance().getConfigDir().resolve("emergentcraft/agents.tsv");
    public AgentRegistry() { load(); }
    public Collection<AgentRecord> all() { return List.copyOf(byName.values()); }
    public Optional<AgentRecord> get(String name) { return Optional.ofNullable(byName.get(name.toLowerCase(Locale.ROOT))); }
    public AgentRecord create(String name) { if (get(name).isPresent()) throw new IllegalArgumentException("Name already exists: " + name); AgentRecord record = new AgentRecord(UUID.randomUUID(), name, Instant.now(), true, false, null, 0); put(record); return record; }
    public void put(AgentRecord record) { byName.put(record.name().toLowerCase(Locale.ROOT), record); save(); }
    public void remove(String name) { byName.remove(name.toLowerCase(Locale.ROOT)); save(); }
    private void load() { try { if (!Files.exists(file)) return; for (String line : Files.readAllLines(file)) { String[] p=line.split("\\t", -1); if (p.length != 7) continue; byName.put(p[1].toLowerCase(Locale.ROOT), new AgentRecord(UUID.fromString(p[0]), p[1], Instant.parse(p[2]), Boolean.parseBoolean(p[3]), Boolean.parseBoolean(p[4]), p[5].isBlank()?null:UUID.fromString(p[5]), Long.parseLong(p[6]))); } } catch (Exception ignored) { EmergentCraftMod.LOGGER.error("Cannot read agent registry", ignored); } }
    private void save() { try { Files.createDirectories(file.getParent()); List<String> lines=new ArrayList<>(); for (AgentRecord r:byName.values()) lines.add(r.id()+"\t"+r.name()+"\t"+r.createdAt()+"\t"+r.alive()+"\t"+r.paused()+"\t"+(r.embodimentId()==null?"":r.embodimentId())+"\t"+r.epoch()); Files.write(file, lines); } catch (IOException e) { throw new IllegalStateException("Cannot persist agent registry", e); } }
}

