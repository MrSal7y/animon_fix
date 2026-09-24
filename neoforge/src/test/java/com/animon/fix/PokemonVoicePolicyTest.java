package com.animon.fix;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PokemonVoicePolicyTest {
    @Test void syncedEntityOwnerOverridesIncompleteClientPokemonData() {
        assertFalse(PokemonVoicePolicy.isWild(UUID.randomUUID(), false, true));
    }
    @Test void tamedEntityIsNotWildEvenWithoutOwnerData() {
        assertFalse(PokemonVoicePolicy.isWild(null, true, true));
    }
    @Test void unownedUntamedWildPokemonKeepAmbient() {
        assertTrue(PokemonVoicePolicy.isWild(null, false, true));
        assertFalse(PokemonVoicePolicy.isWild(null, false, false));
    }
    @Test void nearbyDifferentSpeciesCannotStealTheSoundSource() {
        assertTrue(PokemonVoicePolicy.matchesSpecies("pokemon.rattata", "rattata"));
        assertTrue(PokemonVoicePolicy.matchesSpecies("pokemon.rattata.alolan", "rattata"));
        assertFalse(PokemonVoicePolicy.matchesSpecies("pokemon.raticate", "rattata"));
        assertFalse(PokemonVoicePolicy.matchesSpecies("pokemon.pikachu", "pika"));
    }
}
