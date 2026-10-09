package com.animon.fix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecentVoiceTrackerTest {
    @Test void onlyMatchingNearbyVoicesAreSuppressed() {
        var tracker = new RecentVoiceTracker();
        tracker.mark("pokemon.bulbasaur", 10, 20, 30, false, 2000, 1000);
        assertTrue(tracker.contains("pokemon.bulbasaur", 10, 20, 30, false, 1500));
        assertFalse(tracker.contains("pokemon.bulbasaur", 30, 20, 30, false, 1500));
        assertFalse(tracker.contains("pokemon.charmander", 10, 20, 30, false, 1500));
        assertFalse(tracker.contains("pokemon.bulbasaur", 10, 20, 30, true, 1500));
    }
    @Test void playbackWindowsExpireAndClearOnWorldChange() {
        var tracker = new RecentVoiceTracker();
        tracker.mark("pokemon.bulbasaur", 0, 0, 0, false, 2000, 1000);
        assertFalse(tracker.contains("pokemon.bulbasaur", 0, 0, 0, false, 2001));
        tracker.mark("pokemon.bulbasaur", 0, 0, 0, false, 4000, 3000);
        tracker.clear();
        assertFalse(tracker.contains("pokemon.bulbasaur", 0, 0, 0, false, 3000));
    }
}
