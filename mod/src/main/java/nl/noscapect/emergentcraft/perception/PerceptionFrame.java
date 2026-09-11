package nl.noscapect.emergentcraft.perception;

import nl.noscapect.emergentcraft.actions.OfferedAction;
import java.util.Map;

/** Immutable perception plus the non-serialised server handles that validate offered actions. */
public record PerceptionFrame(PerceptionModels.AgentPerception perception, Map<String, OfferedAction> offeredActions) { }
