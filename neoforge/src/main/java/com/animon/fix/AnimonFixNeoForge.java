package com.animon.fix;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod("animon_fix")
public final class AnimonFixNeoForge {
    private static final KeyMapping CONFIG_KEY = new KeyMapping(
            "key.animon_fix.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_APOSTROPHE,
            "category.animon_fix"
    );

    public AnimonFixNeoForge(IEventBus modEventBus) {
        AnimonFixConfig.load();
        modEventBus.addListener(AnimonFixNeoForge::registerKeys);
        NeoForge.EVENT_BUS.addListener(AnimonFixNeoForge::onClientTick);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CONFIG_KEY);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (CONFIG_KEY.consumeClick()) {
            client.setScreen(new AnimonFixConfigScreen(client.screen));
        }
    }
}
