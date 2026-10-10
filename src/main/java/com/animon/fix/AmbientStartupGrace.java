package com.animon.fix;

/** Drops loading-time ambient requests instead of queueing a burst for later. */
public final class AmbientStartupGrace {
    private static final int GRACE_TICKS = 5 * 20;
    private int remainingTicks = GRACE_TICKS;

    public void reset() {
        remainingTicks = GRACE_TICKS;
    }

    /** Advance once per active gameplay tick, after the loading screen closes. */
    public void tick() {
        if (remainingTicks > 0) remainingTicks--;
    }

    public boolean suppressAmbient() {
        return remainingTicks > 0;
    }
}
