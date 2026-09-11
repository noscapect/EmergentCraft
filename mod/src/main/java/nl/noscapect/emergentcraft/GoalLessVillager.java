package nl.noscapect.emergentcraft;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;

/**
 * Retains Mob.serverAiStep's navigation and controls, but suppresses Villager's
 * Brain tick. {@link #removeFreeWill()} is also called by the embodiment factory
 * to remove registered goals and Brain behaviours.
 */
public final class GoalLessVillager extends Villager {
    public GoalLessVillager(EntityType<? extends Villager> type, Level level) { super(type, level); }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        // Deliberately empty: Mob.serverAiStep still ticks navigation, move/look/jump controls.
        // Villager.customServerAiStep would tick vanilla activity/POI/social behaviours.
    }

    @Override
    public void refreshBrain(ServerLevel level) {
        // A later vanilla refresh must not re-install the behaviour set removed at embodiment.
    }
}
