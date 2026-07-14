package com.animon.fix;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public final class RecentPokemonSoundTracker {
    private static final long SUPPRESSION_WINDOW_MS = 2500L;
    private static final double SAME_SOURCE_DISTANCE_SQUARED = 64.0D;
    private static final Queue<SoundMark> RECENT_MARKS = new ArrayDeque<>();

    private RecentPokemonSoundTracker() {
    }

    public static boolean shouldCancel(SoundInstance sound) {
        Identifier id = sound.getId();
        if (!isCobblemonPokemonAmbient(id)) {
            markIfTrigger(sound, id);
            return false;
        }

        long now = System.currentTimeMillis();
        prune(now);

        for (SoundMark mark : RECENT_MARKS) {
            if (mark.isNear(sound)) {
                return true;
            }
        }

        return false;
    }

    private static void markIfTrigger(SoundInstance sound, Identifier id) {
        String path = id.getPath();
        if (!"cobblemon".equals(id.getNamespace())) {
            return;
        }

        if (isPokemonCry(path) || isPokeBallSendOut(path)) {
            RECENT_MARKS.add(new SoundMark(System.currentTimeMillis(), sound.getX(), sound.getY(), sound.getZ()));
        }
    }

    private static boolean isCobblemonPokemonAmbient(Identifier id) {
        return "cobblemon".equals(id.getNamespace()) && isPokemonAmbient(id.getPath());
    }

    private static boolean isPokemonAmbient(String path) {
        return path.startsWith("pokemon.") && (path.endsWith(".ambient") || path.endsWith("_ambient"));
    }

    private static boolean isPokemonCry(String path) {
        return path.startsWith("pokemon.") && (path.endsWith(".cry") || path.endsWith("_cry"));
    }

    private static boolean isPokeBallSendOut(String path) {
        return "poke_ball_send_out".equals(path) || "poke_ball_shiny_send_out".equals(path);
    }

    private static void prune(long now) {
        Iterator<SoundMark> iterator = RECENT_MARKS.iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().timeMillis > SUPPRESSION_WINDOW_MS) {
                iterator.remove();
            }
        }
    }

    private record SoundMark(long timeMillis, double x, double y, double z) {
        private boolean isNear(SoundInstance sound) {
            double dx = x - sound.getX();
            double dy = y - sound.getY();
            double dz = z - sound.getZ();
            return dx * dx + dy * dy + dz * dz <= SAME_SOURCE_DISTANCE_SQUARED;
        }
    }
}
