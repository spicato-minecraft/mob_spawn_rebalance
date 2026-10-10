package mob_spawn_rebalance.gametest;

import com.mojang.datafixers.util.Either;
import mob_spawn_rebalance.spawn.BreezeSpawnPolicy;
import mob_spawn_rebalance.spawn.PeakPhantomSpawnPolicy;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

/**
 * Mesa breezes use the outdoor surface, and only the plateau. Y 70 is the first allowed feet
 * position; the canyon step just below it is not. Plains are covered by another test.
 */
public class BreezeMesaHeightGate {

	private static final long NIGHT_WITH_SATURATED_DIFFICULTY = 1_530_000L;

	@GameTest(maxTicks = 100, skyAccess = true)
	public void breezeMesaHeightGate(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		level.getServer().setDifficulty(Difficulty.HARD, true);
		setNight(level);

		BlockPos anchor = context.absolutePos(new BlockPos(2, 0, 2));
		int z = anchor.getZ();
		int x70 = anchor.getX();
		int x69 = x70 + 2;
		level.getChunk(x70 >> 4, z >> 4);
		level.getChunk(x69 >> 4, z >> 4);

		// Feet sit in the air on top of the column. Y 70 is the plateau line; Y 69 is the step below it.
		BlockPos feet70 = openSurface(level, x70, z, 69);
		BlockPos feet69 = openSurface(level, x69, z, 68);
		context.assertValueEqual(feet70.getY(), BreezeSpawnPolicy.MESA_PLATEAU_MIN_Y, "plateau feet");
		context.assertValueEqual(feet69.getY(), BreezeSpawnPolicy.MESA_PLATEAU_MIN_Y - 1, "below-plateau feet");

		Holder<Biome> badlands = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.BADLANDS);
		BlockPos min = new BlockPos(Math.min(x70, x69) - 4, 64, z - 4);
		BlockPos max = new BlockPos(Math.max(x70, x69) + 4, 80, z + 4);
		Either<Integer, com.mojang.brigadier.exceptions.CommandSyntaxException> filled =
				FillBiomeCommand.fill(level, min, max, badlands);
		if (filled.right().isPresent()) {
			context.fail("could not set badlands biome: " + filled.right().get().getMessage());
		}
		context.assertTrue(level.getBiome(feet70).is(Biomes.BADLANDS), "Y 70 column should be badlands");
		context.assertTrue(level.getBiome(feet69).is(Biomes.BADLANDS), "Y 69 column should be badlands");
		context.assertTrue(PeakPhantomSpawnPolicy.isOutdoorSurface(level, feet70), "Y 70 should be an outdoor surface, heightmap " + surfaceY(level, feet70));
		context.assertTrue(PeakPhantomSpawnPolicy.isOutdoorSurface(level, feet69), "Y 69 should be an outdoor surface, heightmap " + surfaceY(level, feet69));

		RandomSource random = new DarkEnoughRandom();
		boolean allowed70 = SpawnPlacements.checkSpawnRules(EntityTypes.BREEZE, level, EntitySpawnReason.NATURAL, feet70, random);
		boolean allowed69 = SpawnPlacements.checkSpawnRules(EntityTypes.BREEZE, level, EntitySpawnReason.NATURAL, feet69, random);
		context.assertTrue(
				allowed70,
				"badlands outdoor surface at Y 70 should allow natural breezes (policy="
						+ BreezeSpawnPolicy.allowsNaturalSpawnLocation(level, feet70) + ")"
		);
		context.assertTrue(
				!allowed69,
				"badlands outdoor surface at Y 69 should deny natural breezes (policy="
						+ BreezeSpawnPolicy.allowsNaturalSpawnLocation(level, feet69) + ")"
		);
		context.succeed();
	}

	private static void setNight(ServerLevel level) {
		Holder<WorldClock> overworld = level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD);
		level.clockManager().setTotalTicks(overworld, NIGHT_WITH_SATURATED_DIFFICULTY);
		level.updateSkyBrightness();
	}

	/** Stone at {@code floorY}, air above it, feet in the first air block. */
	private static BlockPos openSurface(ServerLevel level, int x, int z, int floorY) {
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

	private static int surfaceY(ServerLevel level, BlockPos feet) {
		return level.getHeight(Heightmap.Types.WORLD_SURFACE, feet.getX(), feet.getZ());
	}

	/**
	 * Sky light in open air is 15. Vanilla accepts the spawn when {@code nextInt(32)} is at least that,
	 * so the height gate is what decides Y 70 versus Y 69.
	 */
	private static final class DarkEnoughRandom implements RandomSource {
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
