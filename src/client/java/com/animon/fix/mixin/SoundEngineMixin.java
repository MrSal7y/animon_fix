package com.animon.fix.mixin;

import com.animon.fix.ClientPokemonSoundFilter;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Shadow @Final
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Shadow
    public abstract void stop(SoundInstance sound);

    // Delayed/repeating instances also reach play() here. The sound file is already resolved.
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getLocation()Lnet/minecraft/resources/ResourceLocation;"),
            cancellable = true)
    private void animonFix$filterPlayback(SoundInstance sound, CallbackInfo ci) {
        if (instanceToChannel.keySet().stream().anyMatch(active -> ClientPokemonSoundFilter.isOverlappingAmbient(sound, active))
                || ClientPokemonSoundFilter.shouldCancel(sound)) {
            ci.cancel();
            return;
        }
        // Snapshot: replacing a built-in ambient can play a cry recursively.
        for (SoundInstance active : List.copyOf(instanceToChannel.keySet())) {
            if (ClientPokemonSoundFilter.isOverlappingAmbient(active, sound)) stop(active);
        }
    }

    // Scale the raw volume before vanilla clamps it, including Alpha voices and tick updates.
    @Redirect(method = {"play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",
            "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getVolume()F"))
    private float animonFix$voiceVolume(SoundInstance sound) {
        return sound.getVolume() * ClientPokemonSoundFilter.voiceVolume(sound.getLocation());
    }
}
