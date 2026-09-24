package com.animon.fix;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SoundOverlapPolicyTest {
    private boolean overlaps(String ambient, String cry, double distance, boolean relative) {
        return SoundOverlapPolicy.overlaps(ambient, 0, 0, 0, false, cry, distance, 0, 0, relative);
    }
    @Test void matchingVoicesOverlapInEitherArrivalOrder() {
        assertTrue(overlaps("cobblemon:pokemon.pikachu.ambient", "cobblemon:pokemon.pikachu.cry", 0, false));
        assertTrue(overlaps("cobblemon:pokemon.pikachu_ambient", "cobblemon:pokemon.pikachu.cry", 1, false));
    }
    @Test void unrelatedPokemonStayAudible() {
        assertFalse(overlaps("cobblemon:pokemon.pikachu.ambient", "cobblemon:pokemon.eevee.cry", 0, false));
        assertFalse(overlaps("cobblemon:pokemon.pikachu.ambient", "cobblemon:pokemon.pikachu.cry", 5, false));
    }
    @Test void battleMusicIsNotFiltered() {
        assertFalse(overlaps("cobblethemes:battle.pikachu", "cobblemon:pokemon.pikachu.cry", 0, false));
        assertFalse(overlaps("cobblemon:pokemon.pikachu.ambient", "cobblemon:battle.pvw.default", 0, false));
        assertFalse(overlaps("cobblemon:pokemon.pikachu.ambient", "cobblemon:pokemon.pikachu.battle", 0, false));
    }
    @Test void uiAndWorldSoundsDoNotMatch() {
        assertFalse(overlaps("cobblemon:pokemon.pikachu.ambient", "cobblemon:pokemon.pikachu.cry", 0, true));
    }
}
