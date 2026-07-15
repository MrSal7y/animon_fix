package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockSoundKeyframe;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedrockSoundKeyframe.class)
public abstract class BedrockSoundKeyframeMixin {
    @Shadow
    public abstract Identifier getSound();

    @Inject(
            method = "run(Lnet/minecraft/entity/Entity;Lcom/cobblemon/mod/common/client/render/models/blockbench/PosableState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void animonFix$cancelAmbientDuringCry(Entity entity, PosableState state, CallbackInfo ci) {
        if (!(entity instanceof PokemonEntity) || !isPokemonAmbientSound(this.getSound())) {
            return;
        }

        if (CryAnimationTracker.shouldSuppressAmbient(entity)) {
            ci.cancel();
        }
    }

    private static boolean isPokemonAmbientSound(Identifier id) {
        String path = id.getPath();
        return "cobblemon".equals(id.getNamespace())
                && path.startsWith("pokemon.")
                && (path.endsWith(".ambient") || path.endsWith("_ambient"));
    }
}
