package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityAmbientSoundMixin {
    @Inject(method = "playSound(Lnet/minecraft/sound/SoundEvent;FF)V", at = @At("HEAD"), cancellable = true)
    private void animonFix$cancelNonWildPokemonAmbient(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof PokemonEntity pokemonEntity)) {
            return;
        }

        Identifier id = sound.getId();
        if (!"cobblemon".equals(id.getNamespace()) || !isPokemonAmbientSound(id.getPath())) {
            return;
        }

        if (CryAnimationTracker.shouldSuppressAmbient(pokemonEntity)) {
            ci.cancel();
        }
    }

    private static boolean isPokemonAmbientSound(String path) {
        return path.startsWith("pokemon.") && (path.endsWith(".ambient") || path.endsWith("_ambient"));
    }
}
