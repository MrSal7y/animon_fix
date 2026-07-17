package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BattleCryScheduler {
    private static final long NORMAL_CRY_SUPPRESSION_MS = 1500L;
    private static final Map<UUID, Boolean> BATTLE_STATES = new HashMap<>();
    private static final Map<String, Long> RECENT_BATTLE_CRIES = new HashMap<>();

    private BattleCryScheduler() {
    }

    public static void tick(Minecraft client) {
        if (client.level == null) {
            BATTLE_STATES.clear();
            RECENT_BATTLE_CRIES.clear();
            return;
        }

        long now = System.currentTimeMillis();
        prune(now);

        Set<UUID> seen = new HashSet<>();
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof PokemonEntity pokemonEntity)) {
                continue;
            }

            UUID uuid = entity.getUUID();
            seen.add(uuid);
            boolean battling = pokemonEntity.isBattling();
            boolean wasBattling = BATTLE_STATES.getOrDefault(uuid, false);
            BATTLE_STATES.put(uuid, battling);

            if (battling && !wasBattling && AnimonFixConfig.battleCries()) {
                playBattleCry(client, pokemonEntity);
            }
        }

        BATTLE_STATES.keySet().removeIf(uuid -> !seen.contains(uuid));
    }

    public static boolean shouldCancelNormalCry(SoundInstance sound) {
        if (!AnimonFixConfig.battleCries()) {
            return false;
        }

        ResourceLocation id = sound.getLocation();
        String path = id.getPath();
        if (!"cobblemon".equals(id.getNamespace()) || !path.startsWith("pokemon.") || !(path.endsWith(".cry") || path.endsWith("_cry"))) {
            return false;
        }

        Long expiresAt = RECENT_BATTLE_CRIES.get(basePokemonSoundPath(id));
        return expiresAt != null && expiresAt >= System.currentTimeMillis();
    }

    private static void playBattleCry(Minecraft client, PokemonEntity pokemonEntity) {
        ResourceLocation battleSound = getBattleSoundId(pokemonEntity);
        RECENT_BATTLE_CRIES.put(basePokemonSoundPath(battleSound), System.currentTimeMillis() + NORMAL_CRY_SUPPRESSION_MS);
        client.level.playLocalSound(
                pokemonEntity.getX(),
                pokemonEntity.getY(),
                pokemonEntity.getZ(),
                SoundEvent.createVariableRangeEvent(battleSound),
                pokemonEntity.getSoundSource(),
                1.0F,
                1.0F,
                false
        );
    }

    private static ResourceLocation getBattleSoundId(PokemonEntity pokemonEntity) {
        String species = pokemonEntity.getPokemon().getSpecies().getResourceIdentifier().getPath();
        return ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon." + species + ".battle");
    }

    private static void prune(long now) {
        Iterator<Map.Entry<String, Long>> iterator = RECENT_BATTLE_CRIES.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() < now) {
                iterator.remove();
            }
        }
    }

    private static String basePokemonSoundPath(ResourceLocation id) {
        String path = id.getPath();
        if (path.endsWith(".battle") || path.endsWith(".cry")) {
            return path.substring(0, path.lastIndexOf('.'));
        }
        if (path.endsWith("_battle")) {
            return path.substring(0, path.length() - "_battle".length());
        }
        if (path.endsWith("_cry")) {
            return path.substring(0, path.length() - "_cry".length());
        }
        return path;
    }
}
