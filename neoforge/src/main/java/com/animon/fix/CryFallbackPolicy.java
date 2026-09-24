package com.animon.fix;

import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation;
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockActiveAnimation;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockSoundKeyframe;

public final class CryFallbackPolicy {
    private CryFallbackPolicy() {}

    public static boolean needsFallback(boolean wild, ActiveAnimation animation) {
        if (wild) return false;
        while (animation instanceof PrimaryAnimation primary) {
            animation = primary.getAnimation();
        }
        if (!(animation instanceof BedrockActiveAnimation bedrock)) {
            // Unknown/custom animations may produce audio themselves.
            return false;
        }
        String name = bedrock.getAnimation().getName();
        return name != null && name.contains("cry")
                && bedrock.getAnimation().getEffects().stream()
                    .noneMatch(effect -> effect instanceof BedrockSoundKeyframe);
    }
}
