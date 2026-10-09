package mob_spawn_rebalance.spawn;

import mob_spawn_rebalance.MobSpawnRebalance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

import java.util.Set;

/**
 * Peak-only natural phantom spawns. Heavily weighted so the three peaks are a hunting ground.
 * Insomnia / {@code PhantomSpawner} is not used. Natural attempts must be the outdoor
 * world-surface column (not cave floors) and then hover {@link #HOVER_OFFSET} blocks up if that
 * air is empty.
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

	/**
	 * Peak hunting-ground weight. Vanilla creeper/skeleton are 100; other peak hostiles sum
	 * to roughly 600, so phantoms are about half of the night monster lottery.
	 */
	public static final int SPAWN_WEIGHT = 600;
	/** Same pack size as vanilla zombie/creeper/skeleton. */
	public static final int SPAWN_MIN_GROUP = 4;
	public static final int SPAWN_MAX_GROUP = 4;
	/** Natural phantoms appear this many blocks above the outdoor surface. */
	public static final int HOVER_OFFSET = 20;

	private PeakPhantomSpawnPolicy() {
	}

	public static BlockPos hoverPos(BlockPos surfaceFeet) {
		return surfaceFeet.above(HOVER_OFFSET);
	}

	/**
	 * {@code worldSurfaceY} is {@link LevelReader#getHeight(Heightmap.Types, int, int)} for
	 * {@link Heightmap.Types#WORLD_SURFACE} — the first air block above the outdoor top.
	 * {@code ON_GROUND} feet are that air, or one below after {@code adjustSpawnPosition}.
	 */
	public static boolean isOutdoorSurfaceY(int feetY, int worldSurfaceY) {
		return feetY == worldSurfaceY || feetY == worldSurfaceY - 1;
	}

	public static boolean isOutdoorSurface(LevelReader level, BlockPos feet) {
		if (!level.canSeeSky(feet)) {
			return false;
		}
		int worldSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, feet.getX(), feet.getZ());
		return isOutdoorSurfaceY(feet.getY(), worldSurfaceY);
	}

	public static boolean isHoverSpaceClear(LevelReader level, BlockPos hover) {
		if (level.isOutsideBuildHeight(hover) || !level.getWorldBorder().isWithinBounds(hover)) {
			return false;
		}
		BlockState state = level.getBlockState(hover);
		FluidState fluid = level.getFluidState(hover);
		return NaturalSpawner.isValidEmptySpawnBlock(level, hover, state, fluid, EntityTypes.PHANTOM);
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
				.anyMatch(type -> type == EntityTypes.PHANTOM);
	}
}
