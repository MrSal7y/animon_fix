package com.animon.fix;

/** Exact Fabric built-in pack IDs; resource packs named after Cobblemon remain overrides. */
public final class AmbientResourcePolicy {
    private AmbientResourcePolicy() {}

    public static boolean isBuiltIn(String packId) {
        return "cobblemon".equals(packId) || "fabric".equals(packId);
    }
}
