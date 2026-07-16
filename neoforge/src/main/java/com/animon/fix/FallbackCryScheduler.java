package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
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

    public static void schedule(Entity entity) {
        if (!(entity instanceof PokemonEntity pokemonEntity)) {
            return;
        }

        ResourceLocation soundId = getCrySoundId(pokemonEntity);
        PENDING_CRIES.put(entity.getUUID(), new PendingCry(entity.getId(), soundId, System.currentTimeMillis() + FALLBACK_DELAY_MS));
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

            Entity entity = client.level.getEntity(pendingCry.entityId);
            if (!(entity instanceof PokemonEntity pokemonEntity) || entity.isRemoved()) {
                continue;
            }

            client.level.playLocalSound(
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvent.createVariableRangeEvent(pendingCry.soundId),
                    pokemonEntity.getSoundSource(),
                    1.0F,
                    1.0F,
                    false
            );
        }
    }

    private static ResourceLocation getCrySoundId(PokemonEntity pokemonEntity) {
        String species = pokemonEntity.getPokemon().getSpecies().getResourceIdentifier().getPath();
        return ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon." + species + ".cry");
    }

    private record PendingCry(int entityId, ResourceLocation soundId, long playAt) {
    }
}
