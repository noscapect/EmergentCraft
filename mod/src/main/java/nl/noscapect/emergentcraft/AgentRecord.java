package nl.noscapect.emergentcraft;

import java.time.Instant;
import java.util.UUID;

/** Persistent identity, not a claim that the linked entity still exists. */
public record AgentRecord(UUID id, String name, Instant createdAt, boolean alive, boolean paused, UUID embodimentId, long epoch) {
    public AgentRecord withEmbodiment(UUID entityId) { return new AgentRecord(id, name, createdAt, alive, paused, entityId, epoch); }
    public AgentRecord withPaused(boolean value) { return new AgentRecord(id, name, createdAt, alive, value, embodimentId, epoch + 1); }
    public AgentRecord dead() { return new AgentRecord(id, name, createdAt, false, true, embodimentId, epoch + 1); }
    public AgentRecord nextEpoch() { return new AgentRecord(id, name, createdAt, alive, paused, embodimentId, epoch + 1); }
}

