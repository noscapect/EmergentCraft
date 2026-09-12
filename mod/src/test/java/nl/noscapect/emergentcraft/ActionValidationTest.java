package nl.noscapect.emergentcraft;

import nl.noscapect.emergentcraft.actions.ActionValidation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActionValidationTest {
    @Test void unbreakable_or_remote_or_stale_blocks_are_rejected() { assertFalse(ActionValidation.canBreak(-1,1,true)); assertFalse(ActionValidation.canBreak(1,10,true)); assertFalse(ActionValidation.canBreak(1,1,false)); assertTrue(ActionValidation.canBreak(1,9,true)); }
    @Test void pickup_requires_the_offered_item_to_be_near_and_fit() { assertFalse(ActionValidation.canPickUp(1,false,true)); assertFalse(ActionValidation.canPickUp(10,true,true)); assertFalse(ActionValidation.canPickUp(1,true,false)); assertTrue(ActionValidation.canPickUp(1,true,true)); }
    @Test void attack_and_placement_need_offered_reachable_physical_targets() { assertTrue(ActionValidation.canAttack(9,true,true)); assertFalse(ActionValidation.canAttack(9,false,true)); assertFalse(ActionValidation.canAttack(10,true,true)); assertTrue(ActionValidation.canPlace(true,true,true,true,9)); assertFalse(ActionValidation.canPlace(true,false,true,true,9)); assertFalse(ActionValidation.canPlace(true,true,false,true,9)); }
    @Test void build_plan_is_bounded_and_inventory_local() { assertTrue(ActionValidation.boundedBuildPlan(24,true,true)); assertFalse(ActionValidation.boundedBuildPlan(25,true,true)); assertFalse(ActionValidation.boundedBuildPlan(2,false,true)); }
}
