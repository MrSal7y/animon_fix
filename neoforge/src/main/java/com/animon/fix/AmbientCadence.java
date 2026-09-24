package com.animon.fix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Limits incoming ambient requests; never schedules or generates sounds. */
public final class AmbientCadence {
    private final Map<UUID, Long> lastPlayed = new HashMap<>();

    public boolean allow(UUID pokemon, long tick, int minimumInterval) {
        long interval = Math.max(0, minimumInterval);
        lastPlayed.values().removeIf(previous -> tick < previous || tick - previous >= interval);
        if (lastPlayed.containsKey(pokemon)) return false;
        lastPlayed.put(pokemon, tick);
        return true;
    }

    public void clear() {
        lastPlayed.clear();
    }
}
