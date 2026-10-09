package com.animon.fix;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class SoundPolicyTest {
    private SoundInstance sound(String id, double x, boolean relative) {
        return (SoundInstance) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{SoundInstance.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLocation" -> ResourceLocation.parse(id);
                    case "getX" -> x;
                    case "getY", "getZ" -> 0.0;
                    case "isRelative" -> relative;
                    default -> throw new AssertionError(method.getName());
                });
    }
    @Test void cryStopsOnlyMatchingNearbyAmbient() {
        assertTrue(ClientPokemonSoundFilter.isOverlappingAmbient(
                sound("cobblemon:pokemon.bulbasaur.ambient", 0, false), sound("cobblemon:pokemon.bulbasaur.cry", 0, false)));
        assertFalse(ClientPokemonSoundFilter.isOverlappingAmbient(
                sound("cobblemon:pokemon.bulbasaur.ambient", 0, false), sound("cobblemon:pokemon.bulbasaur.cry", 20, false)));
        assertFalse(ClientPokemonSoundFilter.isOverlappingAmbient(
                sound("cobblemon:pokemon.charmander.ambient", 0, false), sound("cobblemon:pokemon.bulbasaur.cry", 0, false)));
        assertFalse(ClientPokemonSoundFilter.isOverlappingAmbient(
                sound("cobblemon:pokemon.bulbasaur.ambient", 0, true), sound("cobblemon:pokemon.bulbasaur.cry", 0, false)));
    }
    @Test void optionalBattleVoiceCountsAsACryButMusicDoesNot() {
        assertTrue(ClientPokemonSoundFilter.isOverlappingAmbient(
                sound("cobblemon:pokemon.bulbasaur.ambient", 0, false), sound("cobblemon:pokemon.bulbasaur.battle", 0, false)));
        assertFalse(ClientPokemonSoundFilter.isOverlappingAmbient(
                sound("cobblemon:pokemon.bulbasaur.ambient", 0, false), sound("cobblemon:battle.pvp", 0, false)));
        assertFalse(ClientPokemonSoundFilter.isOverlappingAmbient(
                sound("other:pokemon.bulbasaur.ambient", 0, false), sound("other:pokemon.bulbasaur.cry", 0, false)));
    }
    @Test void underscoreVoicesAndFormNamesArePreserved() {
        assertTrue(ClientPokemonSoundFilter.isOverlappingAmbient(
                sound("cobblemon:pokemon.raichu_alola_ambient", 0, false), sound("cobblemon:pokemon.raichu_alola_cry", 0, false)));
        assertEquals("pokemon.raichu_alola", ClientPokemonSoundFilter.basePokemonSoundPath(ResourceLocation.parse("cobblemon:pokemon.raichu_alola_cry")));
    }
    @Test void userPacksNamedAfterCobblemonAreNotBuiltIn() {
        assertTrue(AmbientResourcePolicy.isBuiltIn("cobblemon"));
        assertTrue(AmbientResourcePolicy.isBuiltIn("fabric"));
        assertFalse(AmbientResourcePolicy.isBuiltIn("file/Cobblemon Animon.zip"));
        assertFalse(AmbientResourcePolicy.isBuiltIn("Cobblemon Voices"));
        assertFalse(AmbientResourcePolicy.isBuiltIn("mod_resources"));
    }
}
