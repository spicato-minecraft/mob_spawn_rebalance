package mob_spawn_rebalance.spawn;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;

/**
 * Adds phantoms to the peak-biome {@link MobCategory#MONSTER} pool and leaves spawning to NaturalSpawner.
 */
public final class PeakPhantomSpawns {

	private PeakPhantomSpawns() {
	}

	public static void register() {
		BiomeModifications.addSpawn(
				BiomeSelectors.includeByKey(PeakPhantomSpawnPolicy.PEAK_BIOMES),
				MobCategory.MONSTER,
				EntityTypes.PHANTOM,
				PeakPhantomSpawnPolicy.SPAWN_WEIGHT,
				PeakPhantomSpawnPolicy.SPAWN_MIN_GROUP,
				PeakPhantomSpawnPolicy.SPAWN_MAX_GROUP
		);
	}
}
