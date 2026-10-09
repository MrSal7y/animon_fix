package com.animon.fix;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class CryAnimationTracker {
    private static final long CRY_START_WINDOW_MS = 5000L;
    private static final Map<UUID, Long> CRY_WINDOWS = new HashMap<>();

    private CryAnimationTracker() {
    }

    public static void markCryStarted(Entity entity) {
        long now = System.currentTimeMillis();
        if (entity == null) {
            return;
        }
        prune(now);
        CRY_WINDOWS.put(entity.getUUID(), now + CRY_START_WINDOW_MS);
    }

    public static boolean shouldSuppressAmbient(Entity entity) {
        if (entity == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        prune(now);
        Long expiresAt = CRY_WINDOWS.get(entity.getUUID());
        return expiresAt != null && expiresAt >= now;
    }

    private static void prune(long now) {
        Iterator<Map.Entry<UUID, Long>> iterator = CRY_WINDOWS.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() < now) {
                iterator.remove();
            }
        }
    }

    public static void clear() {
        CRY_WINDOWS.clear();
    }
}
