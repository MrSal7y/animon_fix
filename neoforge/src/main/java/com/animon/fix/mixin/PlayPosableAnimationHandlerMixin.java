package com.animon.fix.mixin;

import com.animon.fix.CryAnimationTracker;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.cobblemon.mod.common.client.net.animation.PlayPosableAnimationHandler")
public abstract class PlayPosableAnimationHandlerMixin {
    @Inject(method = "handle(Lcom/cobblemon/mod/common/net/messages/client/animation/PlayPosableAnimationPacket;Lnet/minecraft/client/Minecraft;)V", at = @At("HEAD"))
    private void animonFix$trackCryAnimationPacket(PlayPosableAnimationPacket packet, Minecraft client, CallbackInfo ci) {
        if (client.level == null || packet.getAnimation().stream().noneMatch(animation -> animation.contains("cry"))) {
            return;
        }

        Entity entity = client.level.getEntity(packet.getEntityId());
        if (entity instanceof PokemonEntity) {
            CryAnimationTracker.markCryStarted(entity);
        }
    }
}
