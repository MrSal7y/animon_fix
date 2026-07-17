package com.animon.fix;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AnimonFixConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("animon-soundfix.properties");

    private static boolean pokemonAmbientSounds = true;
    private static int ambientVoiceVolume = 100;
    private static int cryVoiceVolume = 100;
    private static boolean battleCries = false;

    private AnimonFixConfig() {
    }

    public static void load() {
        Properties properties = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                properties.load(input);
            } catch (IOException ignored) {
            }
        }

        pokemonAmbientSounds = Boolean.parseBoolean(properties.getProperty("pokemonAmbientSounds", "true"));
        ambientVoiceVolume = parseVolume(properties.getProperty("ambientVoiceVolume"), 100);
        cryVoiceVolume = parseVolume(properties.getProperty("cryVoiceVolume"), 100);
        battleCries = Boolean.parseBoolean(properties.getProperty("battleCries", "false"));
        save();
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("pokemonAmbientSounds", Boolean.toString(pokemonAmbientSounds));
        properties.setProperty("ambientVoiceVolume", Integer.toString(ambientVoiceVolume));
        properties.setProperty("cryVoiceVolume", Integer.toString(cryVoiceVolume));
        properties.setProperty("battleCries", Boolean.toString(battleCries));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "Animon SoundFix client settings");
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean pokemonAmbientSounds() {
        return pokemonAmbientSounds;
    }

    public static void setPokemonAmbientSounds(boolean value) {
        pokemonAmbientSounds = value;
        save();
    }

    public static float ambientVoiceVolume() {
        return ambientVoiceVolume / 100.0F;
    }

    public static int ambientVoiceVolumePercent() {
        return ambientVoiceVolume;
    }

    public static void adjustAmbientVoiceVolume(int amount) {
        ambientVoiceVolume = clampVolume(ambientVoiceVolume + amount);
        if (ambientVoiceVolume == 0) {
            pokemonAmbientSounds = false;
        } else {
            pokemonAmbientSounds = true;
        }
        save();
    }

    public static float cryVoiceVolume() {
        return cryVoiceVolume / 100.0F;
    }

    public static int cryVoiceVolumePercent() {
        return cryVoiceVolume;
    }

    public static void adjustCryVoiceVolume(int amount) {
        cryVoiceVolume = clampVolume(cryVoiceVolume + amount);
        save();
    }

    public static boolean battleCries() {
        return battleCries;
    }

    public static void setBattleCries(boolean value) {
        battleCries = value;
        save();
    }

    public static void useDefaultCobblemonMode() {
        pokemonAmbientSounds = false;
        ambientVoiceVolume = 0;
        cryVoiceVolume = 100;
        battleCries = false;
        save();
    }

    private static int parseVolume(String value, int fallback) {
        try {
            return clampVolume(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clampVolume(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
