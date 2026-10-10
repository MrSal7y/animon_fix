package com.animon.fix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AmbientStartupGraceTest {
    @Test void loadingRequestsCannotConsumeOrExtendGracePeriod() {
        var grace = new AmbientStartupGrace();
        for (int request = 0; request < 1000; request++) assertTrue(grace.suppressAmbient());
        for (int tick = 0; tick < 99; tick++) grace.tick();
        assertTrue(grace.suppressAmbient());
        grace.tick();
        assertFalse(grace.suppressAmbient());
        for (int tick = 0; tick < 1000; tick++) {
            grace.tick();
            assertFalse(grace.suppressAmbient(), "Normal ambient timing must stay unrestricted");
        }
    }

    @Test void enteringAnotherWorldRestartsGracePeriod() {
        var grace = new AmbientStartupGrace();
        for (int tick = 0; tick < 100; tick++) grace.tick();
        assertFalse(grace.suppressAmbient());
        grace.reset();
        assertTrue(grace.suppressAmbient());
        for (int tick = 0; tick < 99; tick++) grace.tick();
        assertTrue(grace.suppressAmbient());
        grace.tick();
        assertFalse(grace.suppressAmbient());
    }
}
