# Repository workflow

- Fetch `origin` before making requested changes and check for updates to `master`.
- Integrate upstream changes, run the relevant checks, then commit completed task
  changes and push to `origin/master`, unless the user instructs otherwise.
- Keep the NeoForge version at `1.0.3` unless the user explicitly requests a change.
  Leave the Fabric version unchanged when working only on NeoForge.
- Preserve unrelated local edits. Do not commit generated binaries, logs, caches,
  or IDE output as part of a source change.
