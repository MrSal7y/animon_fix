package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.animon.fix.FallbackCryScheduler;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(PosableState.class)
public abstract class PosableStateCryAnimationMixin {
    @Shadow
    public abstract Entity getEntity();

    @Inject(method = "addFirstAnimation(Ljava/util/Set;)V", at = @At("HEAD"))
    private void animonFix$trackQueuedCryAnimation(Set<String> animations, CallbackInfo ci) {
        for (String animation : animations) {
            if (animation.contains("cry")) {
                Entity entity = this.getEntity();
                CryAnimationTracker.markCryStarted(entity);
                FallbackCryScheduler.schedule(entity);
                return;
            }
        }
    }
}
