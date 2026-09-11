package nl.noscapect.emergentcraft;

/** Conservative matching rule for explicitly removing a body that has lost its registry record. */
public final class OrphanedBodyPolicy {
    private OrphanedBodyPolicy() { }

    public static boolean isExactUnregisteredVanillaVillager(String requestedName, String customName, boolean registered, boolean controlledBody, boolean vanillaVillager) {
        return requestedName != null
            && customName != null
            && requestedName.equalsIgnoreCase(customName)
            && !registered
            && !controlledBody
            && vanillaVillager;
    }
}
