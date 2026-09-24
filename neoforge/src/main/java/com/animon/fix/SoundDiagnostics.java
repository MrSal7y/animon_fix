package com.animon.fix;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

/** Opt-in, bounded local trace for diagnosing actual voice playback. */
public final class SoundDiagnostics {
    private static final boolean ENABLED = Files.exists(FMLPaths.CONFIGDIR.get().resolve("animon-soundfix-debug.enabled"));
    private static BufferedWriter writer;
    private static int lines;
    private SoundDiagnostics() {}

    public static void note(String message) {
        if (!ENABLED || lines >= 4000) return;
        try {
            if (writer == null) {
                var path = FMLPaths.GAMEDIR.get().resolve("logs/animon-soundfix-trace.log");
                Files.createDirectories(path.getParent());
                writer = Files.newBufferedWriter(path);
            }
            writer.write(Instant.now() + " " + message);
            writer.newLine();
            writer.flush();
            lines++;
        } catch (IOException ignored) {
            lines = 4000;
        }
    }

    public static void animation(String action, Entity entity, ResourceLocation sound) {
        if (!ENABLED) return;
        note(action + " sound=" + sound + " entity=" + describe(entity));
    }

    public static void playback(String decision, SoundInstance sound) {
        if (!ENABLED || !sound.getLocation().getNamespace().equals("cobblemon")
                || !sound.getLocation().getPath().startsWith("pokemon.")) return;
        var client = Minecraft.getInstance();
        StringBuilder nearby = new StringBuilder();
        if (client.level != null) {
            for (Entity entity : client.level.entitiesForRendering()) {
                if (entity instanceof PokemonEntity && entity.distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) <= 16) {
                    nearby.append(describe(entity)).append(';');
                }
            }
        }
        String pack = "unresolved";
        if (sound.getSound() != null) {
            var file = sound.getSound().getLocation();
            var resource = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), "sounds/" + file.getPath() + ".ogg");
            pack = client.getResourceManager().getResource(resource).map(r -> r.sourcePackId()).orElse("missing");
        }
        String callers = Arrays.stream(Thread.currentThread().getStackTrace())
                .filter(f -> (f.getClassName().startsWith("com.cobblemon.") || f.getClassName().startsWith("com.animon."))
                        && !f.getClassName().equals(SoundDiagnostics.class.getName()))
                .limit(8).map(f -> f.getClassName() + "." + f.getMethodName()).collect(Collectors.joining(" <- "));
        note(decision + " event=" + sound.getLocation() + " file=" + (sound.getSound() == null ? "none" : sound.getSound().getLocation())
                + " pack=" + pack + " relative=" + sound.isRelative() + " category=" + sound.getSource()
                + " xyz=" + sound.getX() + "," + sound.getY() + "," + sound.getZ()
                + " nearby=" + nearby + " via=" + callers);
    }

    private static String describe(Entity entity) {
        if (entity == null) return "null";
        if (entity instanceof PokemonEntity pokemon) {
            return entity.getId() + ":" + pokemon.getPokemon().getSpecies().getResourceIdentifier()
                    + ":wild=" + pokemon.getPokemon().isWild() + ":owned=" + (pokemon.getPokemon().getOwnerUUID() != null)
                    + ":entityOwned=" + (pokemon.getOwnerUUID() != null) + ":tamed=" + pokemon.isTame()
                    + ":battle=" + pokemon.isBattling() + ":tick=" + entity.tickCount;
        }
        return Integer.toString(entity.getId());
    }
}
