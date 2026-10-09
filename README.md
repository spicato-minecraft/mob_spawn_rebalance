# Mob Spawn Rebalance

Fabric mod for Minecraft **26.2** that reworks overworld spawn ecology. Phantoms hunt **peak biomes** at night like other hostiles — they no longer use the sleepless-nights countdown. Breezes also appear at night on those peaks and on **mesa plateaus**.

Elite phantom **loot** still lives in [Mob Loot Rebalance](../mob_loot_rebalance).

## Compatibility

| Component     | Version           |
|---------------|-------------------|
| Minecraft     | 26.2              |
| Fabric Loader | 0.19.3+           |
| Fabric API    | 0.161.0+26.2      |
| Java          | 25+               |

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

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.2.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2 and place it in your mods folder.
3. Copy the built JAR into your Minecraft `mods` folder.

## Branch Policy

Each supported Minecraft version has its own long-lived branch named after the exact `minecraft_version` in `gradle.properties`. Version branches are the source of truth; `main` is not used.

- **Default branch:** `26.2`
- **Feature/fix work:** branch from the target version as `{version}/feature-name`, merge back into the version branch

```bash
git clone https://github.com/spicato-minecraft/mob_spawn_rebalance.git
cd mob_spawn_rebalance
git checkout 26.2
```

## Releases and Modrinth

Merging code does **not** publish to Modrinth. Publishing happens only when a GitHub Release is created.

Release tags use `{minecraft_version}-{mod_version}` (e.g. `26.2-1.0.0`), matching `gradle.properties`.

1. Merge your changes into the version branch (e.g. `26.2`).
2. Bump `mod_version` in `gradle.properties` if shipping new player-facing changes.
3. Add a changelog at `releases/{tag}.md` — see `releases/TEMPLATE.md`.
4. Create a GitHub Release with tag `{minecraft_version}-{mod_version}` targeting the version branch.

See [minecraft-mods/docs/README.md](../docs/README.md) for org-wide release conventions.

## Test Strategy

| Layer      | Purpose                                      |
|------------|----------------------------------------------|
| Unit tests | Peak biome set, spawn weight/group size, outdoor-surface Y, hover offset; breeze biomes and mesa plateau Y |
| GameTests  | Peak monster pools; hover 20 blocks up when air is empty; no spawn when blocked; breeze pools on peaks and mesas |

Behavior tests are tracked in Notion Tasks. Manual UAT items live in the Mod Catalog.

## Architecture

Vanilla `PhantomSpawner` (insomnia / time-since-rest) is cancelled. Phantoms are added to the `MONSTER` spawn pool in `jagged_peaks`, `frozen_peaks`, and `stony_peaks` only (not snowy slopes, groves, meadows, or windswept hills). Weight is **600** (6× creeper/skeleton) so those peaks play as a phantom hunting ground; group size stays **4–4**. Natural attempts must be the outdoor world-surface (sky + heightmap — not cave floors under the peaks). If the air **20 blocks** above that surface is empty, the phantom appears there; if that space is occupied, the attempt fails instead of spawning on the ground.

Breezes join the same peak `MONSTER` pools, plus `badlands`, `wooded_badlands`, and `eroded_badlands`. Weight is **200** with group size **1–2** (common on those surfaces, not a full takeover). Natural attempts use darkness like other hostiles, must be the outdoor world-surface, and in mesa biomes must be at **Y 70 or higher** so they spawn on terracotta tablelands rather than canyon floors. Trial-chamber spawners are unchanged.
