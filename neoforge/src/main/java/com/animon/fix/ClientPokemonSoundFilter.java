package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientPokemonSoundFilter {
    private static final long RECENT_CRY_MS = 1000L;
    private static final double MATCH_DISTANCE_SQUARED = 16.0D;
    private static final Map<String, Long> RECENT_CRIES = new HashMap<>();

    private ClientPokemonSoundFilter() {
    }

    public static boolean shouldCancel(SoundInstance sound) {
        ResourceLocation id = sound.getLocation();
        if (!isPokemonSound(id)) {
            return false;
        }

        if (isPokemonCry(id)) {
            markCry(id);
            if (!AnimonFixConfig.resourceCries()) {
                return playOriginalCobblemonCry(sound);
            }
            return BattleCryScheduler.shouldCancelNormalCry(sound);
        }

        if (!isPokemonAmbient(id)) {
            return false;
        }

        if (!AnimonFixConfig.pokemonAmbientSounds()) {
            return true;
        }

        return isRecentMatchingCry(id) || isOwnedOrBattlingPokemonAtSound(sound);
    }

    public static boolean hasRecentCry(ResourceLocation id) {
        return isRecentMatchingCry(id);
    }

    private static void markCry(ResourceLocation id) {
        long now = System.currentTimeMillis();
        prune(now);
        RECENT_CRIES.put(basePokemonSoundPath(id), now + RECENT_CRY_MS);
    }

    private static boolean isRecentMatchingCry(ResourceLocation id) {
        long now = System.currentTimeMillis();
        prune(now);
        Long expiresAt = RECENT_CRIES.get(basePokemonSoundPath(id));
        return expiresAt != null && expiresAt >= now;
    }

    private static boolean isOwnedOrBattlingPokemonAtSound(SoundInstance sound) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return false;
        }

        double x = sound.getX();
        double y = sound.getY();
        double z = sound.getZ();
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof PokemonEntity pokemonEntity)) {
                continue;
            }

            if (entity.distanceToSqr(x, y, z) > MATCH_DISTANCE_SQUARED) {
                continue;
            }

            if (pokemonEntity.isBattling()
                    || CryAnimationTracker.shouldSuppressAmbient(entity)
                    || pokemonEntity.getPokemon().getOwnerUUID() != null
                    || !pokemonEntity.getPokemon().isWild()) {
                return true;
            }
        }

        return false;
    }

    private static boolean playOriginalCobblemonCry(SoundInstance sound) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return false;
        }

        ResourceLocation originalCry = ResourceLocation.fromNamespaceAndPath("animon_fix", "original." + sound.getLocation().getPath());
        if (!client.getSoundManager().getAvailableSounds().contains(originalCry)) {
            return false;
        }

        client.level.playLocalSound(
                sound.getX(),
                sound.getY(),
                sound.getZ(),
                SoundEvent.createVariableRangeEvent(originalCry),
                sound.getSource(),
                1.0F,
                1.0F,
                false
        );
        return true;
    }

    private static void prune(long now) {
        Iterator<Map.Entry<String, Long>> iterator = RECENT_CRIES.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() < now) {
                iterator.remove();
            }
        }
    }

    private static boolean isPokemonSound(ResourceLocation id) {
        return "cobblemon".equals(id.getNamespace()) && id.getPath().startsWith("pokemon.");
    }

    private static boolean isPokemonAmbient(ResourceLocation id) {
        String path = id.getPath();
        return path.endsWith(".ambient") || path.endsWith("_ambient");
    }

    private static boolean isPokemonCry(ResourceLocation id) {
        String path = id.getPath();
        return path.endsWith(".cry") || path.endsWith("_cry");
    }

    private static String basePokemonSoundPath(ResourceLocation id) {
        String path = id.getPath();
        if (path.endsWith(".ambient") || path.endsWith(".cry")) {
            return path.substring(0, path.lastIndexOf('.'));
        }
        if (path.endsWith("_ambient")) {
            return path.substring(0, path.length() - "_ambient".length());
        }
        if (path.endsWith("_cry")) {
            return path.substring(0, path.length() - "_cry".length());
        }
        return path;
    }
}
