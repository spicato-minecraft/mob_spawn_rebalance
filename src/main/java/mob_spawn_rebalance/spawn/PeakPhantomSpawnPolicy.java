package mob_spawn_rebalance.spawn;

import mob_spawn_rebalance.MobSpawnRebalance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.Set;

/**
 * Peak-only natural phantom spawns. Matches creeper/skeleton monster-pool weight and group size.
 * Insomnia / {@code PhantomSpawner} is not used.
 */
public final class PeakPhantomSpawnPolicy {

	public static final TagKey<Biome> PHANTOM_PEAKS = TagKey.create(
			Registries.BIOME,
			Identifier.fromNamespaceAndPath(MobSpawnRebalance.MOD_ID, "phantom_peaks")
	);

	public static final Set<ResourceKey<Biome>> PEAK_BIOMES = Set.of(
			Biomes.JAGGED_PEAKS,
			Biomes.FROZEN_PEAKS,
			Biomes.STONY_PEAKS
	);

	/** Same weight as vanilla creeper/skeleton in {@code BiomeDefaultFeatures.monsters}. */
	public static final int SPAWN_WEIGHT = 100;
	/** Same pack size as vanilla zombie/creeper/skeleton. */
	public static final int SPAWN_MIN_GROUP = 4;
	public static final int SPAWN_MAX_GROUP = 4;

	private PeakPhantomSpawnPolicy() {
	}

	public static boolean isPeakBiomeKey(ResourceKey<Biome> biome) {
		return PEAK_BIOMES.contains(biome);
	}

	public static boolean allowsNaturalPhantomSpawn(Holder<Biome> biome) {
		if (biome.is(PHANTOM_PEAKS)) {
			return true;
		}
		return biome.unwrapKey().filter(PeakPhantomSpawnPolicy::isPeakBiomeKey).isPresent();
	}

	public static boolean monsterPoolContainsPhantom(Holder<Biome> biome) {
		return biome.value()
				.getMobSettings()
				.getMobs(MobCategory.MONSTER)
				.unwrap()
				.stream()
				.map(weighted -> weighted.value())
				.map(MobSpawnSettings.SpawnerData::type)
				.anyMatch(type -> type == EntityType.PHANTOM);
	}
}
