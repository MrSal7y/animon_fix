package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.Cobblemon;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientPokemonSoundFilter {
    private static final long RECENT_CRY_MS = 1000L;
    private static final double MATCH_DISTANCE_SQUARED = 16.0D;
    private static final AmbientCadence AMBIENT_CADENCE = new AmbientCadence();
    private static ClientLevel soundLevel;
    private static final Map<String, Long> RECENT_CRIES = new HashMap<>();

    private ClientPokemonSoundFilter() {
    }

    public static boolean shouldCancel(SoundInstance sound) {
        Minecraft client = Minecraft.getInstance();
        if (soundLevel != client.level) {
            soundLevel = client.level;
            AMBIENT_CADENCE.clear();
            RECENT_CRIES.clear();
        }
        ResourceLocation id = sound.getLocation();
        if (!isPokemonSound(id)) {
            return false;
        }

        if (isPokemonCry(id)) {
            markCry(id);
            return BattleCryScheduler.shouldCancelNormalCry(sound);
        }

        if (!isPokemonAmbient(id)) {
            return false;
        }

        if (!AnimonFixConfig.pokemonAmbientSounds()) {
            return true;
        }

        PokemonEntity source = findPokemonAtSound(sound);
        if (isRecentMatchingCry(id) || (source != null && (source.isBattling()
                || !PokemonVoicePolicy.isWild(source) || CryAnimationTracker.shouldSuppressAmbient(source)))) {
            SoundDiagnostics.playback("CANCEL_OWNED_OR_CRY", sound);
            return true;
        }
        if (source != null && client.level != null && !AMBIENT_CADENCE.allow(source.getUUID(),
                client.level.getGameTime(), Cobblemon.INSTANCE.getConfig().getAmbientPokemonCryTicks())) {
            SoundDiagnostics.playback("CANCEL_AMBIENT_CADENCE", sound);
            return true;
        }

        return usesOnlyCobblemonSoundResource(sound) && playCryForBuiltInAmbient(sound);
    }

    public static boolean isOverlappingAmbient(SoundInstance ambient, SoundInstance cry) {
        return SoundOverlapPolicy.overlaps(
                ambient.getLocation().toString(), ambient.getX(), ambient.getY(), ambient.getZ(), ambient.isRelative(),
                cry.getLocation().toString(), cry.getX(), cry.getY(), cry.getZ(), cry.isRelative());
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

    private static PokemonEntity findPokemonAtSound(SoundInstance sound) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || sound.isRelative()) return null;
        PokemonEntity closest = null;
        double closestDistance = MATCH_DISTANCE_SQUARED;
        String base = basePokemonSoundPath(sound.getLocation());
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof PokemonEntity pokemon)) continue;
            if (!PokemonVoicePolicy.matchesSpecies(base,
                    pokemon.getPokemon().getSpecies().getResourceIdentifier().getPath())) continue;
            double distance = entity.distanceToSqr(sound.getX(), sound.getY(), sound.getZ());
            if (distance <= closestDistance) {
                closestDistance = distance;
                closest = pokemon;
            }
        }
        return closest;
    }

    private static boolean playCryForBuiltInAmbient(SoundInstance sound) {
        ResourceLocation cryId = toCrySoundId(sound.getLocation());
        if (!hasSoundEvent(cryId)) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return true;
        }

        client.level.playLocalSound(
                sound.getX(),
                sound.getY(),
                sound.getZ(),
                SoundEvent.createVariableRangeEvent(cryId),
                sound.getSource(),
                AnimonFixConfig.cryVoiceVolume(),
                1.0F,
                false
        );
        return true;
    }

    private static boolean usesOnlyCobblemonSoundResource(SoundInstance instance) {
        // SoundEngine has already resolved the instance. Never randomly choose a
        // second variant just to decide whether the sound being played is overridden.
        Sound selectedSound = instance.getSound();
        if (selectedSound == null || selectedSound == SoundManager.EMPTY_SOUND
                || selectedSound == SoundManager.INTENTIONALLY_EMPTY_SOUND) {
            return false;
        }
        ResourceLocation file = selectedSound.getLocation();
        ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(
                file.getNamespace(), "sounds/" + file.getPath() + ".ogg");
        return Minecraft.getInstance().getResourceManager().getResource(resourceId)
                .map(resource -> AmbientResourcePolicy.isBuiltIn(resource.sourcePackId()))
                .orElse(false);
    }

    private static boolean hasSoundEvent(ResourceLocation id) {
        return Minecraft.getInstance().getSoundManager().getAvailableSounds().contains(id);
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

    private static ResourceLocation toCrySoundId(ResourceLocation id) {
        String path = id.getPath();
        if (path.endsWith(".ambient")) {
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), path.substring(0, path.length() - ".ambient".length()) + ".cry");
        }
        if (path.endsWith("_ambient")) {
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), path.substring(0, path.length() - "_ambient".length()) + "_cry");
        }
        return id;
    }
}
