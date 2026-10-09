package com.animon.fix;

import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class FallbackCryScheduler {
    private static final long FALLBACK_DELAY_MS = 250L;
    private static final Map<UUID, PendingCry> PENDING_CRIES = new LinkedHashMap<>();

    private FallbackCryScheduler() {
    }

    public static void schedule(Entity entity, ActiveAnimation animation) {
        if (!(entity instanceof PokemonEntity pokemonEntity)) {
            return;
        }

        if (!CryFallbackPolicy.needsFallback(PokemonVoicePolicy.isWild(pokemonEntity), animation)) {
            // A replacement animation with its own audio supersedes any pending fallback.
            PENDING_CRIES.remove(entity.getUUID());
            return;
        }
        ResourceLocation soundId = getCrySoundId(pokemonEntity);
        PENDING_CRIES.putIfAbsent(entity.getUUID(), new PendingCry(entity, soundId, System.currentTimeMillis() + FALLBACK_DELAY_MS));
    }

    public static void tick(Minecraft client) {
        if (client.level == null || PENDING_CRIES.isEmpty()) {
            PENDING_CRIES.clear();
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, PendingCry>> iterator = PENDING_CRIES.entrySet().iterator();
        while (iterator.hasNext()) {
            PendingCry pendingCry = iterator.next().getValue();
            if (pendingCry.playAt > now) {
                continue;
            }

            iterator.remove();
            if (ClientPokemonSoundFilter.hasRecentCry(pendingCry.soundId, pendingCry.entity)) {
                continue;
            }

            Entity entity = pendingCry.entity;
            if (!(entity instanceof PokemonEntity pokemonEntity) || entity.isRemoved() || entity.level() != client.level
                    || PokemonVoicePolicy.isWild(pokemonEntity)) {
                continue;
            }

            if (!client.getSoundManager().getAvailableSounds().contains(pendingCry.soundId)) {
                continue;
            }
            client.getSoundManager().play(PokemonSoundPlayback.create(pokemonEntity, pendingCry.soundId));
        }
    }

    public static void clear() { PENDING_CRIES.clear(); }

    private static ResourceLocation getCrySoundId(PokemonEntity pokemonEntity) {
        String species = pokemonEntity.getPokemon().getSpecies().getResourceIdentifier().getPath();
        return ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon." + species + ".cry");
    }

    private record PendingCry(Entity entity, ResourceLocation soundId, long playAt) {
    }
}
