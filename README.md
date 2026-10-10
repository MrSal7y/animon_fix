# Animon SoundFix

A client-side Cobblemon addon for Minecraft 1.21.1, available for Fabric and
NeoForge. It is designed for the Animon resource pack and other packs that
provide separate Pokémon ambient voices and cries.

## Compatibility

- Fabric release `1.0.4`: Cobblemon `1.8.0` or `1.8.1`, Minecraft `1.21.1`,
  Java 21, Fabric Loader `0.17.2` or newer, and Fabric API `0.116.6+1.21.1`
  or newer. Cobblemon `1.7.x` is no longer supported by this Fabric release.
- NeoForge release `1.0.3` retains its existing Cobblemon `1.7.3` target.

## Sound behaviour

- Prevents owned or battling Pokémon from playing ambient voices over their cries.
- Preserves resource-pack ambient voices for wild Pokémon.
- On Fabric, skips ambient calls during loading and the first five seconds of
  active gameplay after entering a world or dimension to prevent a loading burst.
  Normal ambient timing then resumes; cries and other game sounds are unaffected.
- On NeoForge, gives each wild Pokémon a random quiet period and a chance to skip
  ambient calls for a more natural mix. Calls stay at least Cobblemon’s configured
  ambient interval apart, without generating extra sounds or changing send-out cries.
- Uses a default Pokémon cry when an ambient sound has no resource-pack override.
- Keeps cry audio synchronised with its animation.
- Supplies a fallback for owned Pokémon whose cry animation has no sound keyframe.
  This does not add cries to wild Pokémon or play ahead of a delayed
  sound keyframe.
- Checks delayed sounds when they play and stops matching nearby
  ambient audio that is already playing when a cry begins.
- On Fabric, applies volume settings to Cobblemon 1.8 Alpha voices as well as
  ordinary ambient voices, animation cries, fallback cries, and battle voices.
- Supports optional battle cries from sound events such as
  `cobblemon:pokemon.bulbasaur.battle`. Battle cries are disabled by default.

## Settings

Open the in-game settings using the configurable keybind in Controls. The default
key is `#`.

You can enable or disable Pokémon ambient sounds, adjust ambient and cry volume,
enable battle cries, and reset settings to their defaults. Volume buttons adjust
by 1%, or 10% while holding Shift.

The addon runs on the client and can be used on servers that do not have it
installed.

## Building and checking Fabric

With JDK 21 installed, run `./gradlew :build` (or `gradlew.bat :build` on Windows).
Use the leading `:` to build only Fabric. Dependencies are downloaded automatically;
no local Cobblemon jar path is needed. The installable jar is
`build/libs/animon-fix-fabric-1.0.4.jar`.

The default dependency is Cobblemon Fabric 1.8.1. To run the same build and regression
checks against 1.8.0, use `./gradlew :build -Pcobblemon_fabric_version=YgmyyFcs`.
Modrinth version IDs select the Fabric jar explicitly because Fabric and NeoForge
share version numbers. `./gradlew :runClient` starts an isolated development client
with Cobblemon installed.

Regression tests check all mandatory mixin targets against the selected release,
delayed playback, Alpha sound routing, late and missing animation sound keyframes,
client ownership data, resource-pack identification, and nearby voice overlap.

For an in-game audio check with your resource pack, try wild, owned, and Alpha
Pokémon; send out a Pokémon with a delayed cry sound keyframe; enter a battle with
battle cries both off and on; and check ambient and cry volume at 100%, 50%, and 0%.
Wild custom ambient voices should remain audible, owned/battling ambient voices
should be suppressed, and delayed cry keyframes should not receive an early fallback.
