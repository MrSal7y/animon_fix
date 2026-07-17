package com.animon.fix;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class AnimonFixClient implements ClientModInitializer {
    private static KeyBinding configKey;

    @Override
    public void onInitializeClient() {
        AnimonFixConfig.load();
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.animon_fix.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_WORLD_2,
                "category.animon_fix"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                client.setScreen(new AnimonFixConfigScreen(client.currentScreen));
            }
        });
    }
}
