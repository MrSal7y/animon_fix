package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.animon.fix.PokemonVoicePolicy;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityAmbientSoundMixin {
    @Inject(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At("HEAD"), cancellable = true)
    private void animonFix$cancelNonWildPokemonAmbient(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof PokemonEntity pokemonEntity)) {
            return;
        }

        ResourceLocation id = sound.getLocation();
        if (!isPokemonAmbient(id)) {
            return;
        }

        if (!PokemonVoicePolicy.isWild(pokemonEntity) || pokemonEntity.isBattling()
                || CryAnimationTracker.shouldSuppressAmbient(pokemonEntity)) {
            ci.cancel();
        }
    }

    private static boolean isPokemonAmbient(ResourceLocation id) {
        String path = id.getPath();
        return "cobblemon".equals(id.getNamespace()) && path.startsWith("pokemon.")
                && (path.endsWith(".ambient") || path.endsWith("_ambient"));
    }
}
