package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

public final class ClientPokemonSoundFilter {
    private static final long RECENT_CRY_MS = 1000L;
    private static final double MATCH_DISTANCE_SQUARED = 16.0D;
    private static final RecentVoiceTracker RECENT_CRIES = new RecentVoiceTracker();
    private static final AmbientStartupGrace STARTUP_GRACE = new AmbientStartupGrace();
    private static ClientLevel soundWorld;

    private ClientPokemonSoundFilter() {}

    public static void tick(Minecraft client) {
        updateWorld(client);
        // Wait until loading screens close before counting playable client ticks.
        if (client.level != null && client.player != null && client.screen == null
                && client.getOverlay() == null && !client.isPaused()) {
            STARTUP_GRACE.tick();
        }
    }

    private static void updateWorld(Minecraft client) {
        if (soundWorld != client.level) {
            soundWorld = client.level;
            STARTUP_GRACE.reset();
            RECENT_CRIES.clear();
            CryAnimationTracker.clear();
            FallbackCryScheduler.clear();
            BattleCryScheduler.clear();
        }
    }

    public static boolean shouldCancel(SoundInstance sound) {
        // Playback can happen before the first client tick in a newly loaded world.
        updateWorld(Minecraft.getInstance());
        ResourceLocation id = sound.getLocation();
        if (!isPokemonSound(id)) return false;
        if (isPokemonCry(id)) {
            if (BattleCryScheduler.shouldCancelNormalCry(sound)) return true;
            markCry(sound);
            return false;
        }
        if (isPokemonBattle(id)) {
            markCry(sound);
            return false;
        }
        if (!isPokemonAmbient(id)) return false;
        if (STARTUP_GRACE.suppressAmbient() || !AnimonFixConfig.pokemonAmbientSounds()) return true;

        PokemonEntity source = findPokemonAtSound(sound);
        if (hasRecentCry(id, sound.getX(), sound.getY(), sound.getZ(), sound.isRelative())
                || (source != null && (source.isBattling() || !PokemonVoicePolicy.isWild(source)
                || CryAnimationTracker.shouldSuppressAmbient(source)))) {
            return true;
        }
        return usesOnlyCobblemonSoundResource(sound) && playCryForBuiltInAmbient(sound, source);
    }

    public static float voiceVolume(ResourceLocation id) {
        if (!isPokemonSound(id)) return 1.0F;
        if (isPokemonCry(id) || isPokemonBattle(id)) return AnimonFixConfig.cryVoiceVolume();
        if (isPokemonAmbient(id)) return AnimonFixConfig.ambientVoiceVolume();
        return 1.0F;
    }

    public static boolean isOverlappingAmbient(SoundInstance ambient, SoundInstance cry) {
        return isPokemonAmbient(ambient.getLocation()) && (isPokemonCry(cry.getLocation()) || isPokemonBattle(cry.getLocation()))
                && basePokemonSoundPath(ambient.getLocation()).equals(basePokemonSoundPath(cry.getLocation()))
                && ambient.isRelative() == cry.isRelative()
                && RecentVoiceTracker.squaredDistance(ambient.getX(), ambient.getY(), ambient.getZ(),
                        cry.getX(), cry.getY(), cry.getZ()) <= MATCH_DISTANCE_SQUARED;
    }

    public static boolean hasRecentCry(ResourceLocation id, Entity entity) {
        return hasRecentCry(id, entity.getX(), entity.getY(), entity.getZ(), false);
    }

    private static boolean hasRecentCry(ResourceLocation id, double x, double y, double z, boolean relative) {
        return RECENT_CRIES.contains(basePokemonSoundPath(id), x, y, z, relative, System.currentTimeMillis());
    }

    private static void markCry(SoundInstance sound) {
        long now = System.currentTimeMillis();
        RECENT_CRIES.mark(basePokemonSoundPath(sound.getLocation()), sound.getX(), sound.getY(), sound.getZ(),
                sound.isRelative(), now + RECENT_CRY_MS, now);
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

    private static boolean playCryForBuiltInAmbient(SoundInstance sound, PokemonEntity source) {
        ResourceLocation cryId = toCrySoundId(sound.getLocation());
        Minecraft client = Minecraft.getInstance();
        if (!client.getSoundManager().getAvailableSounds().contains(cryId) || client.level == null) return false;
        if (source != null) {
            client.getSoundManager().play(PokemonSoundPlayback.create(source, cryId));
        } else {
            client.level.playLocalSound(sound.getX(), sound.getY(), sound.getZ(), SoundEvent.createVariableRangeEvent(cryId),
                    sound.getSource(), sound.getVolume(), sound.getPitch(), false);
        }
        return true;
    }

    private static boolean usesOnlyCobblemonSoundResource(SoundInstance instance) {
        // Use the file selected for this playback, including its highest-priority override.
        Sound selected = instance.getSound();
        if (selected == null || selected == SoundManager.EMPTY_SOUND
                || selected == SoundManager.INTENTIONALLY_EMPTY_SOUND) return false;
        ResourceLocation file = selected.getLocation();
        ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), "sounds/" + file.getPath() + ".ogg");
        return Minecraft.getInstance().getResourceManager().getResource(resource)
                .map(value -> AmbientResourcePolicy.isBuiltIn(value.sourcePackId())).orElse(false);
    }

    static boolean isPokemonSound(ResourceLocation id) {
        return "cobblemon".equals(id.getNamespace()) && id.getPath().startsWith("pokemon.");
    }

    static boolean isPokemonAmbient(ResourceLocation id) {
        return isPokemonSound(id) && (id.getPath().endsWith(".ambient") || id.getPath().endsWith("_ambient"));
    }

    static boolean isPokemonCry(ResourceLocation id) {
        return isPokemonSound(id) && (id.getPath().endsWith(".cry") || id.getPath().endsWith("_cry"));
    }

    static boolean isPokemonBattle(ResourceLocation id) {
        return isPokemonSound(id) && (id.getPath().endsWith(".battle") || id.getPath().endsWith("_battle"));
    }

    static String basePokemonSoundPath(ResourceLocation id) {
        String path = id.getPath();
        for (String kind : new String[]{"ambient", "cry", "battle"}) {
            if (path.endsWith("." + kind) || path.endsWith("_" + kind)) {
                return path.substring(0, path.length() - kind.length() - 1);
            }
        }
        return path;
    }

    private static ResourceLocation toCrySoundId(ResourceLocation id) {
        String path = id.getPath();
        String separator = path.endsWith("_ambient") ? "_" : ".";
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), basePokemonSoundPath(id) + separator + "cry");
    }
}
