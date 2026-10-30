package mob_spawn_rebalance.gametest;

import mob_spawn_rebalance.spawn.CreeperSpawnPolicy;
import mob_spawn_rebalance.spawn.PeakPhantomSpawnPolicy;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Half of above-ground creeper packs stay creepers. The other half becomes another monster
 * from that column's pool. Underground packs and monster spawners never take the roll.
 */
public class CreeperSurfaceSpawnGameTest {

	@GameTest(maxTicks = 100, skyAccess = true)
	public void surfaceNaturalKeepsWhenTheRollAllows(GameTestHelper context) {
		BlockPos feet = prepareSurface(context);
		BoundRandom random = new BoundRandom(0);
		Optional<MobSpawnSettings.SpawnerData> redirected = CreeperSpawnPolicy.redirectSurfaceCreeper(
				context.getLevel(), MobCategory.MONSTER, feet, random, Optional.of(creeperPack())
		);
		context.assertTrue(
				redirected.orElseThrow().type() == EntityTypes.CREEPER,
				"outdoor creeper pack should stay when the keep roll is 0, became " + redirected.orElseThrow().type()
		);
		context.assertValueEqual(random.bounds, List.of(CreeperSpawnPolicy.SURFACE_KEEP_DENOMINATOR), "draws");
		context.succeed();
	}

	@GameTest(maxTicks = 100, skyAccess = true)
	public void surfaceMissBecomesAnotherMonster(GameTestHelper context) {
		BlockPos feet = prepareSurface(context);
		ServerLevel level = context.getLevel();
		boolean stillAllowed = SpawnPlacements.checkSpawnRules(
				EntityTypes.CREEPER, level, EntitySpawnReason.NATURAL, feet, new BoundRandom(CreeperSpawnPolicy.SURFACE_KEEP_NUMERATOR)
		);
		context.assertTrue(stillAllowed, "a missed creeper pack is rerolled later, not cancelled in spawn rules");

		BoundRandom random = new BoundRandom(CreeperSpawnPolicy.SURFACE_KEEP_NUMERATOR);
		Optional<MobSpawnSettings.SpawnerData> redirected = CreeperSpawnPolicy.redirectSurfaceCreeper(
				level, MobCategory.MONSTER, feet, random, Optional.of(creeperPack())
		);
		EntityType<?> type = redirected.orElseThrow().type();
		context.assertTrue(type != EntityTypes.CREEPER, "missed surface pack should leave the creeper, became " + type);
		context.assertTrue(
				CreeperSpawnPolicy.canFillSurfaceCreeperSlot(level, feet, type),
				"replacement should be a mob that can use this surface slot, was " + type
		);
		context.assertTrue(
				random.bounds.getFirst() == CreeperSpawnPolicy.SURFACE_KEEP_DENOMINATOR,
				"surface miss should roll the keep check first, draws " + random.bounds
		);
		context.succeed();
	}

	@GameTest(maxTicks = 100, skyAccess = true)
	public void canopyNaturalStillRollsTheSurfaceCheck(GameTestHelper context) {
		ServerLevel level = night(context);
		BlockPos anchor = context.absolutePos(new BlockPos(2, 0, 2));
		level.getChunk(anchor.getX() >> 4, anchor.getZ() >> 4);
		BlockPos feet = openColumn(level, anchor.getX(), anchor.getZ(), 70);
		level.setBlock(feet.above(2), Blocks.OAK_LEAVES.defaultBlockState(), Block.UPDATE_CLIENTS);
		context.assertTrue(CreeperSpawnPolicy.isAboveGround(level, feet), "forest floor should count as above ground");
		context.assertTrue(
				!PeakPhantomSpawnPolicy.isOutdoorSurface(level, feet),
				"leaves should hide the sky, so this is not an open-sky surface"
		);
		BoundRandom random = new BoundRandom(CreeperSpawnPolicy.SURFACE_KEEP_NUMERATOR);
		Optional<MobSpawnSettings.SpawnerData> redirected = CreeperSpawnPolicy.redirectSurfaceCreeper(
				level, MobCategory.MONSTER, feet, random, Optional.of(creeperPack())
		);
		context.assertTrue(
				redirected.orElseThrow().type() != EntityTypes.CREEPER,
				"creeper pack under leaves should reroll, became " + redirected.orElseThrow().type()
		);
		context.assertValueEqual(List.of(random.bounds.getFirst()), List.of(CreeperSpawnPolicy.SURFACE_KEEP_DENOMINATOR), "draws");
		context.succeed();
	}

	@GameTest(maxTicks = 100, skyAccess = true)
	public void caveNaturalDoesNotRollTheSurfaceCheck(GameTestHelper context) {
		BlockPos feet = prepareCave(context);
		context.assertTrue(
				!CreeperSpawnPolicy.isAboveGround(context.getLevel(), feet),
				"roofed column should not count as above ground"
		);
		BoundRandom random = new BoundRandom(CreeperSpawnPolicy.SURFACE_KEEP_NUMERATOR);
		Optional<MobSpawnSettings.SpawnerData> redirected = CreeperSpawnPolicy.redirectSurfaceCreeper(
				context.getLevel(), MobCategory.MONSTER, feet, random, Optional.of(creeperPack())
		);
		context.assertTrue(
				redirected.orElseThrow().type() == EntityTypes.CREEPER,
				"cave creeper pack should stay a creeper, became " + redirected.orElseThrow().type()
		);
		context.assertValueEqual(random.bounds, List.of(), "cave pack should not draw the surface roll");
		context.succeed();
	}

	@GameTest(maxTicks = 100, skyAccess = true)
	public void surfaceSpawnerIgnoresTheKeepRoll(GameTestHelper context) {
		BlockPos feet = prepareSurface(context);
		BoundRandom random = new BoundRandom(CreeperSpawnPolicy.SURFACE_KEEP_NUMERATOR);
		boolean allowed = SpawnPlacements.checkSpawnRules(
				EntityTypes.CREEPER, context.getLevel(), EntitySpawnReason.SPAWNER, feet, random
		);
		context.assertTrue(allowed, "spawner creeper on the surface should ignore the keep roll, draws " + random.bounds);
		context.assertTrue(
				random.bounds.getFirst() != CreeperSpawnPolicy.SURFACE_KEEP_DENOMINATOR,
				"spawner attempt should not draw the surface keep roll, draws " + random.bounds
		);
		context.succeed();
	}

	private static BlockPos prepareSurface(GameTestHelper context) {
		ServerLevel level = night(context);
		BlockPos anchor = context.absolutePos(new BlockPos(2, 0, 2));
		level.getChunk(anchor.getX() >> 4, anchor.getZ() >> 4);
		BlockPos feet = openColumn(level, anchor.getX(), anchor.getZ(), 70);
		context.assertTrue(CreeperSpawnPolicy.isAboveGround(level, feet), "column should be above ground");
		return feet;
	}

	private static BlockPos prepareCave(GameTestHelper context) {
		ServerLevel level = night(context);
		BlockPos anchor = context.absolutePos(new BlockPos(2, 0, 2));
		level.getChunk(anchor.getX() >> 4, anchor.getZ() >> 4);
		BlockPos feet = openColumn(level, anchor.getX(), anchor.getZ(), 70);
		level.setBlock(feet.above(2), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
		return feet;
	}

	private static ServerLevel night(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		level.getServer().setDifficulty(Difficulty.HARD, true);
		BreezeMesaHeightGate.setNight(level);
		return level;
	}

	private static MobSpawnSettings.SpawnerData creeperPack() {
		return new MobSpawnSettings.SpawnerData(EntityTypes.CREEPER, ConstantInt.of(4));
	}

	/** Stone at {@code floorY}, air above it, feet in the first air block. */
	private static BlockPos openColumn(ServerLevel level, int x, int z, int floorY) {
		int top = level.getMaxY();
		for (int y = floorY + 1; y < top; y++) {
			BlockPos pos = new BlockPos(x, y, z);
			if (!level.getBlockState(pos).isAir()) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		level.setBlock(new BlockPos(x, floorY, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
		return new BlockPos(x, floorY + 1, z);
	}

	/**
	 * {@code nextInt(2)} returns {@code surfaceRoll}. Every other bound returns {@code bound - 1},
	 * which is dark enough for vanilla's sky and block light checks.
	 */
	private static final class BoundRandom implements RandomSource {
		private final int surfaceRoll;
		private final List<Integer> bounds = new ArrayList<>();

		private BoundRandom(int surfaceRoll) {
			this.surfaceRoll = surfaceRoll;
		}

		@Override
		public RandomSource fork() {
			return this;
		}

		@Override
		public PositionalRandomFactory forkPositional() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setSeed(long seed) {
		}

		@Override
		public int nextInt() {
			return Integer.MAX_VALUE;
		}

		@Override
		public int nextInt(int bound) {
			bounds.add(bound);
			if (bound == CreeperSpawnPolicy.SURFACE_KEEP_DENOMINATOR) {
				return surfaceRoll;
			}
			return bound - 1;
		}

		@Override
		public long nextLong() {
			return 0L;
		}

		@Override
		public boolean nextBoolean() {
			return true;
		}

		@Override
		public float nextFloat() {
			return 0.0F;
		}

		@Override
		public double nextDouble() {
			return 0.0;
		}

		@Override
		public double nextGaussian() {
			return 0.0;
		}
	}
}
