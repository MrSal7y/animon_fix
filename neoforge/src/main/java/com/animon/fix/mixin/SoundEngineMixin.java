package com.animon.fix.mixin;

import com.animon.fix.ClientPokemonSoundFilter;
import com.animon.fix.SoundDiagnostics;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Shadow @Final
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Shadow
    public abstract void stop(SoundInstance sound);

    // Run at actual playback, including delayed/repeating sounds, after NeoForge's
    // PlaySoundEvent has had a chance to replace or cancel the sound instance,
    // and after resolve() has selected the actual sound file.
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getLocation()Lnet/minecraft/resources/ResourceLocation;"),
            cancellable = true)
    private void animonFix$filterPlayback(SoundInstance sound, CallbackInfo ci) {
        if (ClientPokemonSoundFilter.shouldCancel(sound)
                || instanceToChannel.keySet().stream().anyMatch(active ->
                    ClientPokemonSoundFilter.isOverlappingAmbient(sound, active))) {
            SoundDiagnostics.playback("CANCEL", sound);
            ci.cancel();
            return;
        }
        SoundDiagnostics.playback("ALLOW", sound);
        // Ambient may already have started before the cry packet/animation arrived.
        // Stop just that nearby matching voice, never battle music or other species.
        for (SoundInstance active : instanceToChannel.keySet()) {
            if (ClientPokemonSoundFilter.isOverlappingAmbient(active, sound)) {
                SoundDiagnostics.playback("STOP_OVERLAP", active);
                stop(active);
            }
        }
    }
}
