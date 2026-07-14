# Animon Ambient Cry Fix

Client-side Fabric addon for Cobblemon 1.7.3 on Minecraft 1.21.1.

This fixes resource packs that provide separate Cobblemon Pokemon `ambient` and `cry` sounds by following Cobblemon's cry animation event and suppressing only that same Pokemon's ambient sound while the cry animation is starting. Wild Pokemon still play their normal ambient sounds, and cry audio remains synced to Cobblemon's cry animations.

Built jar:

```text
build/libs/animon-fix-1.0.0.jar
```

Build it again with:

```powershell
.\gradlew.bat build
```

Put the jar in the client `mods` folder alongside Fabric Loader, Fabric API, Cobblemon, and the Animon resource pack.
