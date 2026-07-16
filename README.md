# Animon Ambient Cry Fix

Client-side Fabric and NeoForge addon for Cobblemon 1.7.3 on Minecraft 1.21.1.

This mod is intended to be used alongside the Animon resource pack. It can also help any other Cobblemon resource pack that adds separate Pokemon ambient sounds and cry sounds.

## What It Does

- Stops owned or battling Pokemon from playing their ambient sound on top of their cry sound when they are sent out or enter a cry animation.
- Keeps normal wild Pokemon ambient sounds working.
- Leaves Cobblemon cry sounds alone so cry audio can stay synced with cry animations.
- Adds a fallback cry trigger for Pokemon where Cobblemon has a cry animation but no sound keyframe, allowing resource-pack cries such as `cobblemon:pokemon.<species>.cry` to play.
- Runs client-side only, so it can be used when joining servers that do not have this addon installed.

## Installation

Put the built jar in your client `mods` folder alongside:

- Fabric Loader
- Fabric API
- Cobblemon
- Animon, or another Cobblemon resource pack that enables Pokemon ambient sounds

Built jar:

```text
build/libs/animon-fix-fabric-1.0.0.jar
neoforge/build/libs/animon-fix-neoforge-1.0.0.jar
```

Build it again with:

```powershell
.\gradlew.bat build
```

Forge note: the public Cobblemon 1.7.3 files for Minecraft 1.21.1 are Fabric and NeoForge, so this project currently builds those two platforms.
