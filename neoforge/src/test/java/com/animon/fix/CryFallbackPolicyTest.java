package com.animon.fix;

import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.*;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CryFallbackPolicyTest {
    private BedrockActiveAnimation animation(String name, boolean sound, float soundAt) {
        List<BedrockEffectKeyframe> effects = sound
                ? List.of(new BedrockSoundKeyframe(soundAt, ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon.charmander.cry")))
                : List.of();
        BedrockAnimation animation = new BedrockAnimation(false, 2.0, effects, Map.of());
        animation.setName(name);
        return new BedrockActiveAnimation(animation);
    }
    @Test void lateCharmanderCryDoesNotGetAnEarlyFallback() {
        assertFalse(CryFallbackPolicy.needsFallback(false, animation("animation.charmander.cry", true, 0.5F)));
    }
    @Test void evenVeryLateCryIsNotConsideredMissing() {
        assertFalse(CryFallbackPolicy.needsFallback(false, animation("animation.charizard.cry", true, 1.0833F)));
    }
    @Test void soundlessCryStillGetsAFallback() {
        assertTrue(CryFallbackPolicy.needsFallback(false, animation("animation.test.cry", false, 0)));
    }
    @Test void silentIdleAnimationDoesNotGenerateWildCries() {
        assertFalse(CryFallbackPolicy.needsFallback(false, animation("animation.test.idle", false, 0)));
    }
    @Test void missingOrUnknownAnimationDoesNotInventACry() {
        assertFalse(CryFallbackPolicy.needsFallback(false, null));
    }
    @Test void wildPokemonNeverReceiveSyntheticAnimationCries() {
        assertFalse(CryFallbackPolicy.needsFallback(true, animation("animation.test.cry", false, 0)));
    }
    @Test void wrappedLateCryDoesNotGetAFallback() {
        var primary = new com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation(
                animation("animation.charmander.cry", true, 0.5F), value -> value, java.util.Set.of(), false);
        assertFalse(CryFallbackPolicy.needsFallback(false, primary));
    }
}
