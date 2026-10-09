package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.animon.fix.FallbackCryScheduler;
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonClientDelegate.class)
public abstract class PokemonClientDelegateCryMixin {
    @Shadow
    private ActiveAnimation cryAnimation;
    @Shadow
    public abstract PokemonEntity getCurrentEntity();

    @Inject(method = "cry()V", at = @At("HEAD"))
    private void animonFix$markCryStart(CallbackInfo ci) {
        CryAnimationTracker.markCryStarted(this.getCurrentEntity());
    }

    @Inject(method = "cry()V", at = @At(value = "FIELD",
            target = "Lcom/cobblemon/mod/common/client/entity/PokemonClientDelegate;cryAnimation:Lcom/cobblemon/mod/common/client/render/models/blockbench/animation/ActiveAnimation;",
            opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void animonFix$inspectSelectedCry(CallbackInfo ci) {
        FallbackCryScheduler.schedule(getCurrentEntity(), cryAnimation);
    }
}
