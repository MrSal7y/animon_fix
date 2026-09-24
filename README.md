# Animon SoundFix

A client-side Cobblemon addon for Minecraft 1.21.1, available for Fabric and
NeoForge. It is designed for the Animon resource pack and other packs that
provide separate Pokémon ambient voices and cries.

## Sound behaviour

- Prevents owned or battling Pokémon from playing ambient voices over their cries.
- Preserves resource-pack ambient voices for wild Pokémon.
- On NeoForge, gives each wild Pokémon a random quiet period and a chance to skip
  ambient calls for a more natural mix. Calls stay at least Cobblemon’s configured
  ambient interval apart, without generating extra sounds or changing send-out cries.
- Uses a default Pokémon cry when an ambient sound has no resource-pack override.
- Keeps cry audio synchronised with its animation.
- Supplies a fallback for owned Pokémon whose cry animation has no sound keyframe.
  On NeoForge, this does not add cries to wild Pokémon or play ahead of a delayed
  sound keyframe.
- On NeoForge, checks delayed sounds when they play and stops matching nearby
  ambient audio that is already playing when a cry begins.
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
