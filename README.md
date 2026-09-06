# Mob Spawn Rebalance

Fabric mod for Minecraft **1.21.11** that reworks overworld spawn ecology. Phantoms hunt **peak biomes** at night like other hostiles — they no longer use the sleepless-nights countdown.

Elite phantom **loot** still lives in [Mob Loot Rebalance](../mob_loot_rebalance).

## Compatibility

| Component     | Version           |
|---------------|-------------------|
| Minecraft     | 1.21.11           |
| Fabric Loader | 0.19.3+           |
| Fabric API    | 0.141.6+1.21.11   |
| Java          | 21+               |

## Build

From this directory:

```bash
./gradlew build
```

The output JAR is at `build/libs/mob_spawn_rebalance-1.0.0.jar` (version from `gradle.properties`).

## Test

```bash
./gradlew test          # unit tests (JUnit + Fabric Loader bootstrap)
./gradlew runGameTest   # server GameTests
./gradlew build         # full CI gate (includes both)
```

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11 and place it in your mods folder.
3. Copy the built JAR into your Minecraft `mods` folder.

## Branch Policy

Each supported Minecraft version has its own long-lived branch named after the exact `minecraft_version` in `gradle.properties`. Version branches are the source of truth; `main` is not used.

- **Default branch:** `1.21.11`
- **Feature/fix work:** branch from the target version as `{version}/feature-name`, merge back into the version branch

```bash
git clone https://github.com/spicato-minecraft/mob_spawn_rebalance.git
cd mob_spawn_rebalance
git checkout 1.21.11
```

## Releases and Modrinth

Merging code does **not** publish to Modrinth. Publishing happens only when a GitHub Release is created.

Release tags use `{minecraft_version}-{mod_version}` (e.g. `1.21.11-1.0.0`), matching `gradle.properties`.

1. Merge your changes into the version branch (e.g. `1.21.11`).
2. Bump `mod_version` in `gradle.properties` if shipping new player-facing changes.
3. Add a changelog at `releases/{tag}.md` — see `releases/TEMPLATE.md`.
4. Create a GitHub Release with tag `{minecraft_version}-{mod_version}` targeting the version branch.

See [minecraft-mods/docs/README.md](../docs/README.md) for org-wide release conventions.

## Test Strategy

| Layer      | Purpose                                      |
|------------|----------------------------------------------|
| Unit tests | Peak biome set and spawn weight/group size   |
| GameTests  | Peak monster pools include phantoms; others do not |

Behavior tests are tracked in Notion Tasks. Manual UAT items live in the Mod Catalog.

## Architecture

Vanilla `PhantomSpawner` (insomnia / time-since-rest) is cancelled. Phantoms are added to the `MONSTER` spawn pool in `jagged_peaks`, `frozen_peaks`, and `stony_peaks` only (not snowy slopes, groves, meadows, or windswept hills), at creeper/skeleton weight **100** and group size **4–4**. Spawn placement uses ground + surface-monster rules (darkness and sky access).
