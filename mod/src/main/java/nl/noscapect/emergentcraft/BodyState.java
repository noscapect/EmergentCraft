package nl.noscapect.emergentcraft;

/** Diagnostic distinction between registry state and the currently loaded Minecraft entity. */
public enum BodyState {
    CONTROLLED, VANILLA_NEEDS_RESTORE, UNLOADED, MISSING, INVALID_REGISTRY, WRONG_ENTITY, DEAD
}
