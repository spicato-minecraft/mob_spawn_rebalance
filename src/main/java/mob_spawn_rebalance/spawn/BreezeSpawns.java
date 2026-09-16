package mob_spawn_rebalance.spawn;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

import java.util.Set;

/**
 * Adds breezes to peak and mesa {@link MobCategory#MONSTER} pools. Placement filters live in spawn rules.
 */
public final class BreezeSpawns {

	private BreezeSpawns() {
	}

	public static void register() {
		addTo(BreezeSpawnPolicy.PEAK_BIOMES);
		addTo(BreezeSpawnPolicy.MESA_BIOMES);
	}

	private static void addTo(Set<ResourceKey<Biome>> biomes) {
		BiomeModifications.addSpawn(
				BiomeSelectors.includeByKey(biomes),
				MobCategory.MONSTER,
				EntityType.BREEZE,
				BreezeSpawnPolicy.SPAWN_WEIGHT,
				BreezeSpawnPolicy.SPAWN_MIN_GROUP,
				BreezeSpawnPolicy.SPAWN_MAX_GROUP
		);
	}
}
