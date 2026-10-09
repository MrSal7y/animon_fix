package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BattleCryScheduler {
    private static final long NORMAL_CRY_SUPPRESSION_MS = 1500L;
    private static final Map<UUID, Boolean> BATTLE_STATES = new HashMap<>();
    private static final RecentVoiceTracker RECENT_BATTLE_CRIES = new RecentVoiceTracker();
    private static SoundInstance allowedNormalCry;

    private BattleCryScheduler() {}

    public static void clear() {
        BATTLE_STATES.clear();
        RECENT_BATTLE_CRIES.clear();
        allowedNormalCry = null;
    }

    public static void tick(Minecraft client) {
        if (client.level == null) {
            clear();
            return;
        }
        if (!AnimonFixConfig.battleCries()) RECENT_BATTLE_CRIES.clear();
        Set<UUID> seen = new HashSet<>();
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof PokemonEntity pokemon)) continue;
            UUID uuid = entity.getUUID();
            seen.add(uuid);
            boolean battling = pokemon.isBattling();
            boolean wasBattling = BATTLE_STATES.getOrDefault(uuid, false);
            BATTLE_STATES.put(uuid, battling);
            if (battling && !wasBattling && AnimonFixConfig.battleCries()) playBattleCry(client, pokemon);
        }
        BATTLE_STATES.keySet().removeIf(uuid -> !seen.contains(uuid));
    }

    public static boolean shouldCancelNormalCry(SoundInstance sound) {
        if (!AnimonFixConfig.battleCries() || sound == allowedNormalCry) return false;
        if (!ClientPokemonSoundFilter.isPokemonCry(sound.getLocation())) return false;
        return RECENT_BATTLE_CRIES.contains(ClientPokemonSoundFilter.basePokemonSoundPath(sound.getLocation()),
                sound.getX(), sound.getY(), sound.getZ(), sound.isRelative(), System.currentTimeMillis());
    }

    private static void playBattleCry(Minecraft client, PokemonEntity pokemon) {
        String species = pokemon.getPokemon().getSpecies().getResourceIdentifier().getPath();
        ResourceLocation battle = ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon." + species + ".battle");
        ResourceLocation cry = ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon." + species + ".cry");
        ResourceLocation id = client.getSoundManager().getAvailableSounds().contains(battle) ? battle : cry;
        if (!client.getSoundManager().getAvailableSounds().contains(id)) return;
        // A normal send-out cry may already have played before this tick observes battle entry.
        if (id.equals(cry) && ClientPokemonSoundFilter.hasRecentCry(cry, pokemon)) return;
        SoundInstance sound = PokemonSoundPlayback.create(pokemon, id);
        long now = System.currentTimeMillis();
        RECENT_BATTLE_CRIES.mark(ClientPokemonSoundFilter.basePokemonSoundPath(id),
                pokemon.getX(), pokemon.getY(), pokemon.getZ(), false, now + NORMAL_CRY_SUPPRESSION_MS, now);
        allowedNormalCry = sound;
        try {
            client.getSoundManager().play(sound);
        } finally {
            allowedNormalCry = null;
        }
    }
}
