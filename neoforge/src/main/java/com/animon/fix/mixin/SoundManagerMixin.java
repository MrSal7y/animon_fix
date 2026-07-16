package com.animon.fix.mixin;

import com.animon.fix.ClientPokemonSoundFilter;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void animonFix$cancelImmediateDuplicatePokemonAmbient(SoundInstance sound, CallbackInfo ci) {
        if (ClientPokemonSoundFilter.shouldCancel(sound)) {
            ci.cancel();
        }
    }

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V", at = @At("HEAD"), cancellable = true)
    private void animonFix$cancelDelayedDuplicatePokemonAmbient(SoundInstance sound, int delay, CallbackInfo ci) {
        if (ClientPokemonSoundFilter.shouldCancel(sound)) {
            ci.cancel();
        }
    }
}
