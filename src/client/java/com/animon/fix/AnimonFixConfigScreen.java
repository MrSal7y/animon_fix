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

        this.addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> {
            AnimonFixConfig.adjustAmbientVoiceVolume(step(-1));
            this.clearAndInit();
        }).dimensions(x, y + 24, 32, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ambientVolumeText(), button -> {
        }).dimensions(x + 36, y + 24, 188, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            AnimonFixConfig.adjustAmbientVoiceVolume(step(1));
            this.clearAndInit();
        }).dimensions(x + 228, y + 24, 32, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> {
            AnimonFixConfig.adjustCryVoiceVolume(step(-1));
            this.clearAndInit();
        }).dimensions(x, y + 48, 32, 20).build());

        this.addDrawableChild(ButtonWidget.builder(cryVolumeText(), button -> {
        }).dimensions(x + 36, y + 48, 188, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            AnimonFixConfig.adjustCryVoiceVolume(step(1));
            this.clearAndInit();
        }).dimensions(x + 228, y + 48, 32, 20).build());

        this.addDrawableChild(ButtonWidget.builder(battleCriesText(), button -> {
            AnimonFixConfig.setBattleCries(!AnimonFixConfig.battleCries());
            button.setMessage(battleCriesText());
        }).dimensions(x, y + 72, 260, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Default Cobblemon Mode"), button -> {
            AnimonFixConfig.useDefaultCobblemonMode();
            this.clearAndInit();
        }).dimensions(x, y + 104, 260, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> this.close())
                .dimensions(x, y + 136, 260, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 28, 0xFFFFFF);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Battle cries use sound events like cobblemon:pokemon.bulbasaur.battle."),
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

    private static Text ambientVolumeText() {
        return Text.literal("Ambient Volume: " + AnimonFixConfig.ambientVoiceVolumePercent() + "%");
    }

    private static Text cryVolumeText() {
        return Text.literal("Cry Volume: " + AnimonFixConfig.cryVoiceVolumePercent() + "%");
    }

    private static Text battleCriesText() {
        return Text.literal("Battle Cries: " + onOff(AnimonFixConfig.battleCries()));
    }

    private static String onOff(boolean value) {
        return value ? "On" : "Off";
    }

    private static int step(int direction) {
        return direction * (Screen.hasShiftDown() ? 10 : 1);
    }
}
