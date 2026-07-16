package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

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

        Identifier soundId = getCrySoundId(pokemonEntity);
        PENDING_CRIES.put(entity.getUuid(), new PendingCry(entity.getId(), soundId, System.currentTimeMillis() + FALLBACK_DELAY_MS));
    }

    public static void tick(MinecraftClient client) {
        if (client.world == null || PENDING_CRIES.isEmpty()) {
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

            Entity entity = client.world.getEntityById(pendingCry.entityId);
            if (!(entity instanceof PokemonEntity pokemonEntity) || entity.isRemoved()) {
                continue;
            }

            client.world.playSoundFromEntity(entity, SoundEvent.of(pendingCry.soundId), pokemonEntity.getSoundCategory(), 1.0F, 1.0F);
        }
    }

    private static Identifier getCrySoundId(PokemonEntity pokemonEntity) {
        String species = pokemonEntity.getPokemon().getSpecies().getResourceIdentifier().getPath();
        return Identifier.of("cobblemon", "pokemon." + species + ".cry");
    }

    private record PendingCry(int entityId, Identifier soundId, long playAt) {
    }
}
