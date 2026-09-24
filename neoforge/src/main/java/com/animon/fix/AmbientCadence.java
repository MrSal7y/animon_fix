package com.animon.fix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

/** Limits incoming ambient requests; never schedules or generates sounds. */
public final class AmbientCadence {
    private static final double PLAY_CHANCE = 0.65D;
    private final Map<UUID, Long> nextEligible = new HashMap<>();
    private final DoubleSupplier random;
    private long lastTick = Long.MIN_VALUE;

    public AmbientCadence() {
        this(() -> ThreadLocalRandom.current().nextDouble());
    }

    AmbientCadence(DoubleSupplier random) {
        this.random = random;
    }

    public boolean allow(UUID pokemon, long tick, int minimumInterval) {
        long interval = Math.max(0, minimumInterval);
        if (tick < lastTick || interval == 0) clear();
        lastTick = tick;
        if (interval == 0) return true;

        nextEligible.values().removeIf(deadline -> tick >= deadline);
        if (nextEligible.containsKey(pokemon)) return false;

        boolean play = random.getAsDouble() < PLAY_CHANCE;
        // After playing, keep the proven minimum spacing and add up to one extra
        // interval. A skipped call waits before rerolling, so packet spam cannot
        // immediately undo the choice to stay quiet. Each creature rolls afresh.
        double multiplier = play ? 1.0D + random.getAsDouble() : 0.25D + 0.5D * random.getAsDouble();
        nextEligible.put(pokemon, tick + Math.max(1L, (long) Math.ceil(interval * multiplier)));
        return play;
    }

    public void clear() {
        nextEligible.clear();
        lastTick = Long.MIN_VALUE;
    }
}
