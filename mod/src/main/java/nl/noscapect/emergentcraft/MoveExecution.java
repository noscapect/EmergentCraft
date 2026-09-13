package nl.noscapect.emergentcraft;

import net.minecraft.world.phys.Vec3;

/** World-action bookkeeping; the path itself remains owned by Minecraft Navigation. */
public record MoveExecution(String actionId, long epoch, Vec3 start, Vec3 target, long startTick, long lastProgressTick, double lastDistance, Vec3 lastPosition, boolean diagnostic) {
    public MoveExecution withProgress(long tick, double distance, Vec3 position) { return new MoveExecution(actionId, epoch, start, target, startTick, tick, distance, position, diagnostic); }
}
