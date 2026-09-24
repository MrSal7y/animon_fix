package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.animon.fix.FallbackCryScheduler;
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation;
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
                return;
            }
        }
    }

    // Inspect the animation Cobblemon actually selected, not the list of possible
    // animation names. A late sound keyframe is not a missing sound keyframe.
    @ModifyArg(method = "addFirstAnimation(Ljava/util/Set;)V",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/client/render/models/blockbench/PosableState;addPrimaryAnimation(Lcom/cobblemon/mod/common/client/render/models/blockbench/animation/PrimaryAnimation;)V"),
            index = 0)
    private PrimaryAnimation animonFix$inspectPrimaryCry(PrimaryAnimation animation) {
        FallbackCryScheduler.schedule(this.getEntity(), animation);
        return animation;
    }

    @ModifyArg(method = "addFirstAnimation(Ljava/util/Set;)V",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/client/render/models/blockbench/PosableState;addActiveAnimation$default(Lcom/cobblemon/mod/common/client/render/models/blockbench/PosableState;Lcom/cobblemon/mod/common/client/render/models/blockbench/animation/ActiveAnimation;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)V"),
            index = 1)
    private ActiveAnimation animonFix$inspectActiveCry(ActiveAnimation animation) {
        FallbackCryScheduler.schedule(this.getEntity(), animation);
        return animation;
    }
}
