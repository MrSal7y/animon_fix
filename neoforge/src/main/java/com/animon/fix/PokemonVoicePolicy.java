package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import java.util.UUID;

public final class PokemonVoicePolicy {
    private PokemonVoicePolicy() {}

    public static boolean isWild(PokemonEntity entity) {
        // Client Pokemon objects can lack the storage/owner data present on the server.
        // TamableAnimal's entity ownership is synchronized to the client separately.
        return isWild(entity.getOwnerUUID(), entity.isTame(), entity.getPokemon().isWild());
    }

    static boolean isWild(UUID entityOwner, boolean tamed, boolean pokemonDataWild) {
        return entityOwner == null && !tamed && pokemonDataWild;
    }

    static boolean matchesSpecies(String soundBase, String species) {
        String base = "pokemon." + species;
        return soundBase.equals(base) || soundBase.startsWith(base + ".") || soundBase.startsWith(base + "_");
    }
}
