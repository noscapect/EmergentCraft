package nl.noscapect.emergentcraft;

import net.minecraft.world.entity.Entity;
import java.util.Optional;

public record BodyResolution(BodyState state, Entity entity) {
    public Optional<Entity> loadedEntity() { return Optional.ofNullable(entity); }
    public boolean isControlled() { return state == BodyState.CONTROLLED && entity instanceof GoalLessVillager; }
}
