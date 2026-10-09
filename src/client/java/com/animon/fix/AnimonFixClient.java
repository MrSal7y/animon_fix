package com.animon.fix;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class AnimonFixClient implements ClientModInitializer {
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        AnimonFixConfig.load();
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.animon_fix.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_WORLD_2,
                "category.animon_fix"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.consumeClick()) {
                client.setScreen(new AnimonFixConfigScreen(client.screen));
            }
        });
    }
}
