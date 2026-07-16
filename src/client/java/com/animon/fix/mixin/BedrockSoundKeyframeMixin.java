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
        Identifier sound = this.getSound();
        if (!isPokemonSound(sound) || isPokemonCry(sound)) {
            return;
        }

        if (entity instanceof PokemonEntity pokemonEntity) {
            if (!pokemonEntity.getPokemon().isWild() || CryAnimationTracker.shouldSuppressAmbient(entity)) {
                ci.cancel();
            }
            return;
        }

        if (CryAnimationTracker.isAnyCryActive()) {
            ci.cancel();
        }
    }

    private static boolean isPokemonSound(Identifier id) {
        String path = id.getPath();
        return "cobblemon".equals(id.getNamespace())
                && path.startsWith("pokemon.");
    }

    private static boolean isPokemonCry(Identifier id) {
        String path = id.getPath();
        return path.endsWith(".cry") || path.endsWith("_cry");
    }
}
