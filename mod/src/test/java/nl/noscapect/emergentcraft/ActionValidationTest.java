package nl.noscapect.emergentcraft;

import nl.noscapect.emergentcraft.actions.ActionValidation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActionValidationTest {
    @Test void unbreakable_or_remote_or_stale_blocks_are_rejected() { assertFalse(ActionValidation.canBreak(-1,1,true)); assertFalse(ActionValidation.canBreak(1,10,true)); assertFalse(ActionValidation.canBreak(1,1,false)); assertTrue(ActionValidation.canBreak(1,9,true)); }
    @Test void pickup_requires_the_offered_item_to_be_near_and_fit() { assertFalse(ActionValidation.canPickUp(1,false,true)); assertFalse(ActionValidation.canPickUp(10,true,true)); assertFalse(ActionValidation.canPickUp(1,true,false)); assertTrue(ActionValidation.canPickUp(1,true,true)); }
}
