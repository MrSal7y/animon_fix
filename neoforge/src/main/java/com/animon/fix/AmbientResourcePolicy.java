package com.animon.fix;

/** Only NeoForge's built-in packs qualify for the default-voice fallback. */
public final class AmbientResourcePolicy {
    private AmbientResourcePolicy() {}

    public static boolean isBuiltIn(String packId) {
        // Native NeoForge can expose mod assets through the merged mod_resources pack.
        // User packs (including ones named "Cobblemon ...") must never match by substring.
        return "mod/cobblemon".equals(packId) || "mod_resources".equals(packId);
    }
}
