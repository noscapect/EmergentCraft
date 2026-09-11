package nl.noscapect.emergentcraft;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrphanedBodyPolicyTest {
    @Test void exactUnregisteredVanillaVillagerCanBeRecovered() {
        assertTrue(OrphanedBodyPolicy.isExactUnregisteredVanillaVillager("Rowan", "rowan", false, false, true));
    }

    @Test void registeredOrControlledBodiesAreNeverTreatedAsOrphans() {
        assertFalse(OrphanedBodyPolicy.isExactUnregisteredVanillaVillager("Rowan", "Rowan", true, false, true));
        assertFalse(OrphanedBodyPolicy.isExactUnregisteredVanillaVillager("Rowan", "Rowan", false, true, true));
    }

    @Test void similarlyNamedOrNonVillagerEntitiesAreNeverMatched() {
        assertFalse(OrphanedBodyPolicy.isExactUnregisteredVanillaVillager("Rowan", "Rowan Jr", false, false, true));
        assertFalse(OrphanedBodyPolicy.isExactUnregisteredVanillaVillager("Rowan", "Rowan", false, false, false));
    }
}
