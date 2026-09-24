package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.animon.fix.SoundDiagnostics;
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonClientDelegate.class)
public abstract class PokemonClientDelegateCryMixin {
    @Shadow
    public abstract PokemonEntity getCurrentEntity();

    @Inject(method = "cry()V", at = @At("HEAD"))
    private void animonFix$trackClientDelegateCry(CallbackInfo ci) {
        SoundDiagnostics.animation("DELEGATE_CRY", this.getCurrentEntity(), null);
        CryAnimationTracker.markCryStarted(this.getCurrentEntity());
    }
}
