package com.animon.fix;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AmbientCadenceTest {
    @Test void repeatedIncomingAmbientPacketsRespectConfiguredInterval() {
        var gate = new AmbientCadence(); var entity = UUID.randomUUID();
        assertTrue(gate.allow(entity, 0, 1080));
        assertFalse(gate.allow(entity, 120, 1080));
        assertFalse(gate.allow(entity, 240, 1080));
        assertFalse(gate.allow(entity, 1079, 1080));
        assertTrue(gate.allow(entity, 1080, 1080));
    }
    @Test void suppressedPacketsDoNotPostponeTheNextAllowedVoice() {
        var gate = new AmbientCadence(); var entity = UUID.randomUUID();
        assertTrue(gate.allow(entity, 100, 1080));
        for (int tick = 101; tick < 1180; tick++) assertFalse(gate.allow(entity, tick, 1080));
        assertTrue(gate.allow(entity, 1180, 1080));
    }
    @Test void eachPokemonHasItsOwnTimingEvenForTheSameSpecies() {
        var gate = new AmbientCadence();
        assertTrue(gate.allow(UUID.randomUUID(), 10, 1080));
        assertTrue(gate.allow(UUID.randomUUID(), 10, 1080));
    }
    @Test void worldChangesCanClearPreviousTimers() {
        var gate = new AmbientCadence(); var entity = UUID.randomUUID();
        assertTrue(gate.allow(entity, 100, 1080)); gate.clear();
        assertTrue(gate.allow(entity, 101, 1080));
    }
    @Test void configCanDisableMinimumSpacing() {
        var gate = new AmbientCadence(); var entity = UUID.randomUUID();
        assertTrue(gate.allow(entity, 10, 0));
        assertTrue(gate.allow(entity, 10, 0));
    }
}
