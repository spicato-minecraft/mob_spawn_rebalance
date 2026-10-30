package mob_spawn_rebalance.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Natural creepers keep the vanilla cave rate. Above ground, half of creeper packs still
 * spawn as creepers. The other half is rolled again from the same monster list, without
 * creepers, so the pack becomes another hostile instead of an empty attempt.
 * <p>
 * Above ground is the walkable column ({@link Heightmap.Types#MOTION_BLOCKING_NO_LEAVES}),
 * so forest floors count and cave floors under a roof do not. Underground picks do not draw
 * from {@code random}.
 */
public final class CreeperSpawnPolicy {

	/** Above-ground creeper packs that stay creepers. */
	public static final int SURFACE_KEEP_NUMERATOR = 1;
	public static final int SURFACE_KEEP_DENOMINATOR = 2;

	private CreeperSpawnPolicy() {
	}

	/**
	 * Feet on the no-leaves ground, including under a canopy. A stone roof in the same
	 * column puts the heightmap on that roof, so the cave floor below it is not above ground.
	 */
	public static boolean isAboveGround(LevelReader level, BlockPos feet) {
		int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, feet.getX(), feet.getZ());
		return PeakPhantomSpawnPolicy.isOutdoorSurfaceY(feet.getY(), groundY);
	}

	/**
	 * A surface stand-in has to be able to use this pack. Slimes fail their own surface rules,
	 * and breezes and phantoms only spawn where those policies already allow them.
	 */
	public static boolean canFillSurfaceCreeperSlot(LevelReader level, BlockPos pos, EntityType<?> type) {
		if (type == EntityTypes.CREEPER || type == EntityTypes.SLIME) {
			return false;
		}
		if (type == EntityTypes.BREEZE) {
			return BreezeSpawnPolicy.allowsNaturalSpawnLocation(level, pos);
		}
		if (type == EntityTypes.PHANTOM) {
			return PeakPhantomSpawnPolicy.isOutdoorSurface(level, pos)
					&& PeakPhantomSpawnPolicy.isHoverSpaceClear(level, PeakPhantomSpawnPolicy.hoverPos(pos));
		}
		return true;
	}

	/**
	 * @param aboveGround {@link #isAboveGround}
	 * @return {@code false} when this above-ground creeper pack should be rerolled
	 */
	public static boolean allowsNaturalAttempt(boolean aboveGround, RandomSource random) {
		if (!aboveGround) {
			return true;
		}
		return random.nextInt(SURFACE_KEEP_DENOMINATOR) < SURFACE_KEEP_NUMERATOR;
	}

	public static Optional<MobSpawnSettings.SpawnerData> pickReplacement(
			List<Weighted<MobSpawnSettings.SpawnerData>> entries,
			Predicate<EntityType<?>> allowed,
			RandomSource random
	) {
		List<Weighted<MobSpawnSettings.SpawnerData>> others = new ArrayList<>();
		for (Weighted<MobSpawnSettings.SpawnerData> entry : entries) {
			if (entry.weight() > 0 && allowed.test(entry.value().type())) {
				others.add(entry);
			}
		}
		if (others.isEmpty()) {
			return Optional.empty();
		}
		return WeightedList.of(others).getRandom(random);
	}

	/**
	 * Natural monster packs only. A kept creeper is returned unchanged. A rerolled pack uses
	 * another entry from the position's monster list. If nothing else can stand in, the creeper
	 * pack is kept so the attempt is not dropped.
	 */
	public static Optional<MobSpawnSettings.SpawnerData> redirectSurfaceCreeper(
			ServerLevel level,
			MobCategory category,
			BlockPos pos,
			RandomSource random,
			Optional<MobSpawnSettings.SpawnerData> picked
	) {
		if (category != MobCategory.MONSTER || picked.isEmpty() || picked.get().type() != EntityTypes.CREEPER) {
			return picked;
		}
		if (!isAboveGround(level, pos) || allowsNaturalAttempt(true, random)) {
			return picked;
		}
		WeightedList<MobSpawnSettings.SpawnerData> pool = level.getChunkSource()
				.getGenerator()
				.getMobsAt(level, level.structureManager(), category, pos);
		return pickReplacement(pool.unwrap(), type -> canFillSurfaceCreeperSlot(level, pos, type), random)
				.or(() -> picked);
	}
}
