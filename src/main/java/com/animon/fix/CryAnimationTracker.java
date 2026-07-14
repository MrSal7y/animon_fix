package com.animon.fix;

import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class CryAnimationTracker {
    private static final long CRY_START_WINDOW_MS = 1200L;
    private static final Map<UUID, Long> CRY_WINDOWS = new HashMap<>();

    private CryAnimationTracker() {
    }

    public static void markCryStarted(Entity entity) {
        if (entity == null) {
            return;
        }
        long now = System.currentTimeMillis();
        prune(now);
        CRY_WINDOWS.put(entity.getUuid(), now + CRY_START_WINDOW_MS);
    }

    public static boolean shouldSuppressAmbient(Entity entity) {
        if (entity == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        prune(now);
        Long expiresAt = CRY_WINDOWS.get(entity.getUuid());
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
}
