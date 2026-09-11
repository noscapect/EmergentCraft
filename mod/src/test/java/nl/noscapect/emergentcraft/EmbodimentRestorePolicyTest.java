package nl.noscapect.emergentcraft;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EmbodimentRestorePolicyTest {
    private final UUID registered = UUID.randomUUID();

    @Test void registeredVanillaVillagerNeedsRewrapping() {
        assertTrue(EmbodimentRestorePolicy.shouldReplace(registered, registered, false, true));
    }

    @Test void controlledRegisteredBodyIsLeftAlone() {
        assertFalse(EmbodimentRestorePolicy.shouldReplace(registered, registered, true, true));
    }

    @Test void unrelatedVillagerIsNeverRewrapped() {
        assertFalse(EmbodimentRestorePolicy.shouldReplace(registered, UUID.randomUUID(), false, true));
    }

    @Test void matchingNonVillagerIsNeverRewrapped() {
        assertFalse(EmbodimentRestorePolicy.shouldReplace(registered, registered, false, false));
    }
}
