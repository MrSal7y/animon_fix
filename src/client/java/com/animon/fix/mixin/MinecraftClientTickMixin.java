package com.animon.fix.mixin;

import com.animon.fix.FallbackCryScheduler;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientTickMixin {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void animonFix$tickFallbackCryScheduler(CallbackInfo ci) {
        FallbackCryScheduler.tick((MinecraftClient) (Object) this);
    }
}
