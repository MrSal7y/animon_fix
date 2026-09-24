package com.animon.fix;

/** Matching only Pokemon voice events keeps battle music outside this filter. */
public final class SoundOverlapPolicy {
    private SoundOverlapPolicy() {}

    public static boolean overlaps(String ambient, double ax, double ay, double az, boolean ar,
                                   String cry, double cx, double cy, double cz, boolean cr) {
        String ambientSpecies = species(ambient, "ambient");
        String crySpecies = species(cry, "cry");
        if (ambientSpecies == null || !ambientSpecies.equals(crySpecies) || ar != cr) {
            return false;
        }
        double dx = ax - cx, dy = ay - cy, dz = az - cz;
        return dx * dx + dy * dy + dz * dz <= 16.0D;
    }

    private static String species(String id, String kind) {
        if (!id.startsWith("cobblemon:pokemon.")) {
            return null;
        }
        if (id.endsWith("." + kind) || id.endsWith("_" + kind)) {
            return id.substring(0, id.length() - kind.length() - 1);
        }
        return null;
    }
}
