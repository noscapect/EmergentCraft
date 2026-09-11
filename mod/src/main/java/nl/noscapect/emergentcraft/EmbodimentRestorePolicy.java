package nl.noscapect.emergentcraft;

import java.util.UUID;

/** Pure matching rule: only the registry's exact body UUID may be rewrapped. */
public final class EmbodimentRestorePolicy {
    private EmbodimentRestorePolicy() { }
    public static boolean shouldReplace(UUID registeredBodyId, UUID loadedEntityId, boolean alreadyControlled, boolean isVanillaVillager) {
        return registeredBodyId != null && registeredBodyId.equals(loadedEntityId) && !alreadyControlled && isVanillaVillager;
    }
}
