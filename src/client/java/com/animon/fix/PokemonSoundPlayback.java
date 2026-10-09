package com.animon.fix;

import com.cobblemon.mod.common.client.sound.instances.AlphaCrySoundInstance;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

final class PokemonSoundPlayback {
    private PokemonSoundPlayback() {}

    static SoundInstance create(PokemonEntity pokemon, ResourceLocation id) {
        SoundEvent event = SoundEvent.createVariableRangeEvent(id);
        if (pokemon.getPokemon().isAlpha()) {
            return new AlphaCrySoundInstance(event, pokemon.getSoundSource(), 1.5F, 0.92F,
                    pokemon, pokemon.level().random.nextLong());
        }
        return new SimpleSoundInstance(event, pokemon.getSoundSource(), 1.0F, 1.0F,
                SoundInstance.createUnseededRandom(), pokemon.getX(), pokemon.getY(), pokemon.getZ());
    }
}
