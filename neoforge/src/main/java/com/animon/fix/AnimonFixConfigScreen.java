package com.animon.fix;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AnimonFixConfigScreen extends Screen {
    private final Screen parent;

    public AnimonFixConfigScreen(Screen parent) {
        super(Component.literal("Animon SoundFix"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 130;
        int y = this.height / 2 - 48;

        this.addRenderableWidget(Button.builder(ambientText(), button -> {
            AnimonFixConfig.setPokemonAmbientSounds(!AnimonFixConfig.pokemonAmbientSounds());
            button.setMessage(ambientText());
        }).bounds(x, y, 260, 20).build());

        this.addRenderableWidget(Button.builder(ambientVolumeText(), button -> {
            AnimonFixConfig.adjustAmbientVoiceVolume(-10);
            button.setMessage(ambientVolumeText());
        }).bounds(x, y + 24, 128, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            AnimonFixConfig.adjustAmbientVoiceVolume(10);
            this.rebuildWidgets();
        }).bounds(x + 132, y + 24, 128, 20).build());

        this.addRenderableWidget(Button.builder(cryVolumeText(), button -> {
            AnimonFixConfig.adjustCryVoiceVolume(-10);
            button.setMessage(cryVolumeText());
        }).bounds(x, y + 48, 128, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            AnimonFixConfig.adjustCryVoiceVolume(10);
            this.rebuildWidgets();
        }).bounds(x + 132, y + 48, 128, 20).build());

        this.addRenderableWidget(Button.builder(battleCriesText(), button -> {
            AnimonFixConfig.setBattleCries(!AnimonFixConfig.battleCries());
            button.setMessage(battleCriesText());
        }).bounds(x, y + 72, 260, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Default Cobblemon Mode"), button -> {
            AnimonFixConfig.useDefaultCobblemonMode();
            this.rebuildWidgets();
        }).bounds(x, y + 104, 260, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(x, y + 136, 260, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 28, 0xFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.literal("Battle cries use sound events like cobblemon:pokemon.bulbasaur.battle."),
                this.width / 2,
                48,
                0xA0A0A0
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static Component ambientText() {
        return Component.literal("Pokemon Ambient Sounds: " + onOff(AnimonFixConfig.pokemonAmbientSounds()));
    }

    private static Component ambientVolumeText() {
        return Component.literal("Ambient Volume: " + AnimonFixConfig.ambientVoiceVolumePercent() + "%");
    }

    private static Component cryVolumeText() {
        return Component.literal("Cry Volume: " + AnimonFixConfig.cryVoiceVolumePercent() + "%");
    }

    private static Component battleCriesText() {
        return Component.literal("Battle Cries: " + onOff(AnimonFixConfig.battleCries()));
    }

    private static String onOff(boolean value) {
        return value ? "On" : "Off";
    }
}
