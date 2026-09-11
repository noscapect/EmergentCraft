package nl.noscapect.emergentcraft.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** Server-owned target identity retained until a model selects its opaque candidate id. */
public record OfferedAction(String id, Kind kind, Vec3 destination, UUID entityId, BlockPos blockPos, String expectedTarget) {
    public enum Kind { WAIT, MOVE_TO, APPROACH_ENTITY, APPROACH_BLOCK, BREAK_BLOCK, PICK_UP_ITEM, SPEAK }
}
