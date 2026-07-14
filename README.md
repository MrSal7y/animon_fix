# Animon Ambient Cry Fix

Client-side Fabric addon for Cobblemon 1.7.3 on Minecraft 1.21.1.

This fixes resource packs that provide separate Cobblemon Pokemon `ambient` and `cry` sounds by suppressing an ambient sound only when it starts from the same source right after a Cobblemon cry or Poké Ball send-out sound. Wild Pokemon still play their normal ambient sounds, while player-owned or battle/send-out Pokemon keep the normal cry behavior without also firing the ambient variant.

Built jar:

```text
build/libs/animon-fix-1.0.0.jar
```

Build it again with:

```powershell
.\gradlew.bat build
```

Put the jar in the client `mods` folder alongside Fabric Loader, Fabric API, Cobblemon, and the Animon resource pack.
