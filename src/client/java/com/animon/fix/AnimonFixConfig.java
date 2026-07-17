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
    private static boolean resourceCries = true;
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
        resourceCries = Boolean.parseBoolean(properties.getProperty(
                "resourceCries",
                properties.getProperty("resourcePackCryFallbacks", "true")
        ));
        battleCries = Boolean.parseBoolean(properties.getProperty("battleCries", "false"));
        save();
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("pokemonAmbientSounds", Boolean.toString(pokemonAmbientSounds));
        properties.setProperty("resourceCries", Boolean.toString(resourceCries));
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

    public static boolean resourceCries() {
        return resourceCries;
    }

    public static void setResourceCries(boolean value) {
        resourceCries = value;
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
        resourceCries = false;
        battleCries = false;
        save();
    }
}
