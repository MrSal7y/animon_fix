package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.cobblemon.mod.common.client.net.animation.PlayPosableAnimationHandler")
public abstract class PlayPosableAnimationHandlerMixin {
    @Inject(
            method = "handle(Lcom/cobblemon/mod/common/net/messages/client/animation/PlayPosableAnimationPacket;Lnet/minecraft/client/MinecraftClient;)V",
            at = @At("HEAD")
    )
    private void animonFix$markCryPacket(PlayPosableAnimationPacket packet, MinecraftClient client, CallbackInfo ci) {
        if (client.world == null || !packet.getAnimation().contains("cry")) {
            return;
        }

        Entity entity = client.world.getEntityById(packet.getEntityId());
        if (entity instanceof PokemonEntity) {
            CryAnimationTracker.markCryStarted(entity);
        }
    }
}
