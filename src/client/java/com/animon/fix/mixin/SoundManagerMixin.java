package com.animon.fix.mixin;

import com.animon.fix.RecentPokemonSoundTracker;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void animonFix$cancelDuplicateAmbient(SoundInstance sound, CallbackInfo ci) {
        if (RecentPokemonSoundTracker.shouldCancel(sound)) {
            ci.cancel();
        }
    }
}
