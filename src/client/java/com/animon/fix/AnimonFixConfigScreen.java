package com.animon.fix;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class AnimonFixConfigScreen extends Screen {
    private final Screen parent;

    public AnimonFixConfigScreen(Screen parent) {
        super(Text.literal("Animon SoundFix"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 130;
        int y = this.height / 2 - 48;

        this.addDrawableChild(ButtonWidget.builder(ambientText(), button -> {
            AnimonFixConfig.setPokemonAmbientSounds(!AnimonFixConfig.pokemonAmbientSounds());
            button.setMessage(ambientText());
        }).dimensions(x, y, 260, 20).build());

        this.addDrawableChild(ButtonWidget.builder(fallbackText(), button -> {
            AnimonFixConfig.setResourcePackCryFallbacks(!AnimonFixConfig.resourcePackCryFallbacks());
            button.setMessage(fallbackText());
        }).dimensions(x, y + 24, 260, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Default Cobblemon Mode"), button -> {
            AnimonFixConfig.useDefaultCobblemonMode();
            this.clearAndInit();
        }).dimensions(x, y + 56, 260, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> this.close())
                .dimensions(x, y + 88, 260, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 28, 0xFFFFFF);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Missing cry fallbacks only affect Pokemon without Cobblemon sound keyframes."),
                this.width / 2,
                48,
                0xA0A0A0
        );
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private static Text ambientText() {
        return Text.literal("Pokemon Ambient Sounds: " + onOff(AnimonFixConfig.pokemonAmbientSounds()));
    }

    private static Text fallbackText() {
        return Text.literal("Missing Cry Fallbacks: " + onOff(AnimonFixConfig.resourcePackCryFallbacks()));
    }

    private static String onOff(boolean value) {
        return value ? "On" : "Off";
    }
}
