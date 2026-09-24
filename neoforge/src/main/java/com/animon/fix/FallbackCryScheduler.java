package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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

        if (!CryFallbackPolicy.needsFallback(pokemonEntity.getPokemon().isWild(), animation)) {
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
            if (ClientPokemonSoundFilter.hasRecentCry(pendingCry.soundId)) {
                continue;
            }

            Entity entity = pendingCry.entity;
            if (!(entity instanceof PokemonEntity pokemonEntity) || entity.isRemoved() || entity.level() != client.level) {
                continue;
            }

            if (!client.getSoundManager().getAvailableSounds().contains(pendingCry.soundId)) {
                continue;
            }

            client.level.playLocalSound(
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvent.createVariableRangeEvent(pendingCry.soundId),
                    pokemonEntity.getSoundSource(),
                    AnimonFixConfig.cryVoiceVolume(),
                    1.0F,
                    false
            );
        }
    }

    private static ResourceLocation getCrySoundId(PokemonEntity pokemonEntity) {
        String species = pokemonEntity.getPokemon().getSpecies().getResourceIdentifier().getPath();
        return ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon." + species + ".cry");
    }

    private record PendingCry(Entity entity, ResourceLocation soundId, long playAt) {
    }
}
