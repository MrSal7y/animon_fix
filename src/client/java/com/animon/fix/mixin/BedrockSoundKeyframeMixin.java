package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.animon.fix.PokemonVoicePolicy;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockSoundKeyframe;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedrockSoundKeyframe.class)
public abstract class BedrockSoundKeyframeMixin {
    @Shadow
    public abstract ResourceLocation getSound();

    @Inject(method = "run(Lnet/minecraft/world/entity/Entity;Lcom/cobblemon/mod/common/client/render/models/blockbench/PosableState;)V",
            at = @At("HEAD"), cancellable = true)
    private void animonFix$cancelAmbientDuringCry(Entity entity, PosableState state, CallbackInfo ci) {
        ResourceLocation sound = getSound();
        String path = sound.getPath();
        if (!"cobblemon".equals(sound.getNamespace()) || !path.startsWith("pokemon.")
                || !(path.endsWith(".ambient") || path.endsWith("_ambient"))) {
            return;
        }
        if (entity instanceof PokemonEntity pokemon && (!PokemonVoicePolicy.isWild(pokemon)
                || pokemon.isBattling() || CryAnimationTracker.shouldSuppressAmbient(entity))) {
            ci.cancel();
        }
    }
}
