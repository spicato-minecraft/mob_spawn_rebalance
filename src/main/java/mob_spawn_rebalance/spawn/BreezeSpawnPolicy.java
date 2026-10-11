package mob_spawn_rebalance.spawn;

import mob_spawn_rebalance.MobSpawnRebalance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.Set;

/**
 * Natural breeze spawns on peak summits and mesa tablelands. Trial-chamber spawners are unchanged.
 * Mesa attempts must sit on the outdoor surface at or above {@link #MESA_PLATEAU_MIN_Y} so canyon
 * floors and washes do not count.
 */
public final class BreezeSpawnPolicy {

	public static final TagKey<Biome> BREEZE_PEAKS = TagKey.create(
			Registries.BIOME,
			Identifier.fromNamespaceAndPath(MobSpawnRebalance.MOD_ID, "breeze_peaks")
	);

	public static final TagKey<Biome> BREEZE_MESAS = TagKey.create(
			Registries.BIOME,
			Identifier.fromNamespaceAndPath(MobSpawnRebalance.MOD_ID, "breeze_mesas")
	);

	public static final Set<ResourceKey<Biome>> PEAK_BIOMES = PeakPhantomSpawnPolicy.PEAK_BIOMES;

	public static final Set<ResourceKey<Biome>> MESA_BIOMES = Set.of(
			Biomes.BADLANDS,
			Biomes.WOODED_BADLANDS,
			Biomes.ERODED_BADLANDS
	);

	/**
	 * Common but not a full biome takeover. Vanilla creeper/skeleton are 100; peaks already give
	 * phantoms 600. Group size stays small — breezes hit harder than a zombie pack.
	 */
	public static final int SPAWN_WEIGHT = 200;
	public static final int SPAWN_MIN_GROUP = 1;
	public static final int SPAWN_MAX_GROUP = 2;

	/**
	 * Terracotta tablelands in 1.18+ badlands sit around this height. Eroded canyon floors and
	 * river cuts are lower, so natural breezes stay on the plateau tops.
	 */
	public static final int MESA_PLATEAU_MIN_Y = 70;

	private BreezeSpawnPolicy() {
	}

	public static boolean isPeakBiomeKey(ResourceKey<Biome> biome) {
		return PEAK_BIOMES.contains(biome);
	}

	public static boolean isMesaBiomeKey(ResourceKey<Biome> biome) {
		return MESA_BIOMES.contains(biome);
	}

	public static boolean isPeakBiome(Holder<Biome> biome) {
		if (biome.is(BREEZE_PEAKS)) {
			return true;
		}
		return biome.unwrapKey().filter(BreezeSpawnPolicy::isPeakBiomeKey).isPresent();
	}

	public static boolean isMesaBiome(Holder<Biome> biome) {
		if (biome.is(BREEZE_MESAS)) {
			return true;
		}
		return biome.unwrapKey().filter(BreezeSpawnPolicy::isMesaBiomeKey).isPresent();
	}

	public static boolean isMesaPlateauY(int feetY) {
		return feetY >= MESA_PLATEAU_MIN_Y;
	}

	public static boolean allowsNaturalSpawnLocation(LevelReader level, BlockPos feet) {
		if (!PeakPhantomSpawnPolicy.isOutdoorSurface(level, feet)) {
			return false;
		}
		Holder<Biome> biome = level.getBiome(feet);
		if (isMesaBiome(biome)) {
			return isMesaPlateauY(feet.getY());
		}
		return isPeakBiome(biome);
	}

	public static boolean monsterPoolContainsBreeze(Holder<Biome> biome) {
		return NaturalMobSpawns.monsterPool(biome)
				.unwrap()
				.stream()
				.map(weighted -> weighted.value())
				.map(MobSpawnSettings.SpawnerData::type)
				.anyMatch(type -> type == EntityTypes.BREEZE);
	}
}
