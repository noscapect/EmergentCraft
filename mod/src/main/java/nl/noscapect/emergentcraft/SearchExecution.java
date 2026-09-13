package nl.noscapect.emergentcraft;

import net.minecraft.core.BlockPos;

/** Bounded server-owned physical search state; targets are checked only through normal local perception. */
public record SearchExecution(String actionId, boolean block, String target, int range, BlockPos origin, long startedTick, long deadlineTick, int leg, int maxLegs) {
    public SearchExecution next() { return new SearchExecution(actionId,block,target,range,origin,startedTick,deadlineTick,leg+1,maxLegs); }
}
