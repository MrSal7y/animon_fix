package com.animon.fix;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AmbientCadenceTest {
    @Test void repeatedIncomingAmbientPacketsRespectConfiguredInterval() {
        var gate = new AmbientCadence(() -> 0.0D); var entity = UUID.randomUUID();
        assertTrue(gate.allow(entity, 0, 1080));
        assertFalse(gate.allow(entity, 120, 1080));
        assertFalse(gate.allow(entity, 240, 1080));
        assertFalse(gate.allow(entity, 1079, 1080));
        assertTrue(gate.allow(entity, 1080, 1080));
    }
    @Test void suppressedPacketsDoNotPostponeTheNextAllowedVoice() {
        var gate = new AmbientCadence(() -> 0.0D); var entity = UUID.randomUUID();
        assertTrue(gate.allow(entity, 100, 1080));
        for (int tick = 101; tick < 1180; tick++) assertFalse(gate.allow(entity, tick, 1080));
        assertTrue(gate.allow(entity, 1180, 1080));
    }
    @Test void eachPokemonHasItsOwnTimingEvenForTheSameSpecies() {
        var gate = new AmbientCadence(() -> 0.0D);
        assertTrue(gate.allow(UUID.randomUUID(), 10, 1080));
        assertTrue(gate.allow(UUID.randomUUID(), 10, 1080));
    }
    @Test void worldChangesCanClearPreviousTimers() {
        var gate = new AmbientCadence(() -> 0.0D); var entity = UUID.randomUUID();
        assertTrue(gate.allow(entity, 100, 1080)); gate.clear();
        assertTrue(gate.allow(entity, 101, 1080));
    }
    @Test void configCanDisableMinimumSpacing() {
        var gate = new AmbientCadence(() -> 0.0D); var entity = UUID.randomUUID();
        assertTrue(gate.allow(entity, 10, 0));
        assertTrue(gate.allow(entity, 10, 0));
    }

    @Test void quietPeriodVariesBetweenCreaturesAndNeverShortensTheMinimum() {
        var rolls = new java.util.ArrayDeque<>(java.util.List.of(0.0D, 0.0D, 0.0D, 0.999D, 0.0D, 0.0D, 0.0D, 0.0D));
        var gate = new AmbientCadence(rolls::remove);
        var first = UUID.randomUUID(); var second = UUID.randomUUID();
        assertTrue(gate.allow(first, 0, 1080));
        assertTrue(gate.allow(second, 0, 1080));
        assertFalse(gate.allow(first, 1079, 1080));
        assertFalse(gate.allow(second, 1079, 1080));
        assertTrue(gate.allow(first, 1080, 1080));
        assertFalse(gate.allow(second, 1080, 1080));
        assertFalse(gate.allow(second, 2158, 1080));
        assertTrue(gate.allow(second, 2159, 1080));
        assertTrue(rolls.isEmpty());
    }

    @Test void someCreaturesSkipCallsAndPacketSpamCannotImmediatelyReroll() {
        var rolls = new java.util.ArrayDeque<>(java.util.List.of(0.9D, 0.5D, 0.0D, 0.5D, 0.0D, 0.0D));
        var gate = new AmbientCadence(rolls::remove);
        var quiet = UUID.randomUUID(); var vocal = UUID.randomUUID();
        assertFalse(gate.allow(quiet, 0, 1080));
        assertTrue(gate.allow(vocal, 0, 1080));
        for (int tick = 1; tick < 540; tick++) assertFalse(gate.allow(quiet, tick, 1080));
        assertTrue(gate.allow(quiet, 540, 1080));
        assertTrue(rolls.isEmpty());
    }

    @Test void backwardsGameClockDoesNotLeavePokemonPermanentlyMuted() {
        var gate = new AmbientCadence(() -> 0.0D); var entity = UUID.randomUUID();
        assertTrue(gate.allow(entity, 1000, 1080));
        assertTrue(gate.allow(entity, 10, 1080));
    }
}
