package com.animon.fix;

import java.util.ArrayList;
import java.util.List;

/** Short playback windows scoped to a voice and its position, rather than all of a species. */
final class RecentVoiceTracker {
    private final List<Voice> voices = new ArrayList<>();

    void mark(String base, double x, double y, double z, boolean relative, long expiresAt, long now) {
        prune(now);
        voices.add(new Voice(base, x, y, z, relative, expiresAt));
    }

    boolean contains(String base, double x, double y, double z, boolean relative, long now) {
        prune(now);
        return voices.stream().anyMatch(voice -> voice.base.equals(base) && voice.relative == relative
                && squaredDistance(voice.x, voice.y, voice.z, x, y, z) <= 16.0D);
    }

    static double squaredDistance(double ax, double ay, double az, double bx, double by, double bz) {
        double dx = ax - bx, dy = ay - by, dz = az - bz;
        return dx * dx + dy * dy + dz * dz;
    }

    void clear() {
        voices.clear();
    }

    private void prune(long now) {
        voices.removeIf(voice -> voice.expiresAt < now);
    }

    private record Voice(String base, double x, double y, double z, boolean relative, long expiresAt) {}
}
