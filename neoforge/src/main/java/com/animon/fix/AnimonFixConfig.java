package com.animon.fix;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AnimonFixConfig {
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("animon-soundfix.properties");

    private static boolean pokemonAmbientSounds = true;
    private static boolean resourcePackCryFallbacks = true;

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
        resourcePackCryFallbacks = Boolean.parseBoolean(properties.getProperty("resourcePackCryFallbacks", "true"));
        save();
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("pokemonAmbientSounds", Boolean.toString(pokemonAmbientSounds));
        properties.setProperty("resourcePackCryFallbacks", Boolean.toString(resourcePackCryFallbacks));

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

    public static boolean resourcePackCryFallbacks() {
        return resourcePackCryFallbacks;
    }

    public static void setResourcePackCryFallbacks(boolean value) {
        resourcePackCryFallbacks = value;
        save();
    }

    public static void useDefaultCobblemonMode() {
        pokemonAmbientSounds = false;
        resourcePackCryFallbacks = false;
        save();
    }
}
