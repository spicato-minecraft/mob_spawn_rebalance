package mob_spawn_rebalance.gametest;

import mob_spawn_rebalance.spawn.PeakPhantomSpawnPolicy;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;

public class PeakPhantomSpawnGameTest {

	@GameTest(maxTicks = 100)
	public void peakMonsterPoolsIncludePhantoms(GameTestHelper context) {
		assertPhantomInPool(context, Biomes.JAGGED_PEAKS, true);
		assertPhantomInPool(context, Biomes.FROZEN_PEAKS, true);
		assertPhantomInPool(context, Biomes.STONY_PEAKS, true);
		assertPhantomInPool(context, Biomes.SNOWY_SLOPES, false);
		assertPhantomInPool(context, Biomes.GROVE, false);
		assertPhantomInPool(context, Biomes.MEADOW, false);
		assertPhantomInPool(context, Biomes.CHERRY_GROVE, false);
		assertPhantomInPool(context, Biomes.WINDSWEPT_HILLS, false);
		assertPhantomInPool(context, Biomes.PLAINS, false);
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void peakPhantomEntryMatchesHuntingGroundRates(GameTestHelper context) {
		Holder<Biome> biome = context.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.JAGGED_PEAKS);
		var phantomEntry = biome.value()
				.getMobSettings()
				.getMobs(MobCategory.MONSTER)
				.unwrap()
				.stream()
				.filter(weighted -> weighted.value().type() == EntityTypes.PHANTOM)
				.findFirst()
				.orElse(null);
		context.assertTrue(phantomEntry != null, "jagged_peaks monster pool should include phantom");
		MobSpawnSettings.SpawnerData phantom = phantomEntry.value();
		context.assertTrue(
				phantomEntry.weight() == PeakPhantomSpawnPolicy.SPAWN_WEIGHT,
				"phantom weight should dominate the peak pool (" + PeakPhantomSpawnPolicy.SPAWN_WEIGHT + ")"
		);
		context.assertTrue(
				phantom.minCount() == PeakPhantomSpawnPolicy.SPAWN_MIN_GROUP
						&& phantom.maxCount() == PeakPhantomSpawnPolicy.SPAWN_MAX_GROUP,
				"phantom group size should stay 4-4"
		);
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void naturalPhantomHoversTwentyBlocksWhenAirIsClear(GameTestHelper context) {
		BlockPos relativeFeet = new BlockPos(1, 1, 1);
		context.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
		Phantom phantom = context.spawn(EntityTypes.PHANTOM, relativeFeet.getX(), relativeFeet.getY(), relativeFeet.getZ(), EntitySpawnReason.NATURAL);
		BlockPos expectedHover = context.absolutePos(relativeFeet).above(PeakPhantomSpawnPolicy.HOVER_OFFSET);
		context.assertTrue(phantom.isAlive(), "phantom should spawn when hover air is empty");
		context.assertTrue(
				phantom.blockPosition().equals(expectedHover),
				"natural phantom should hover " + PeakPhantomSpawnPolicy.HOVER_OFFSET + " above the surface, at "
						+ expectedHover + " but was " + phantom.blockPosition()
		);
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void naturalPhantomDoesNotSpawnWhenHoverIsBlocked(GameTestHelper context) {
		BlockPos relativeFeet = new BlockPos(1, 1, 1);
		context.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
		context.setBlock(relativeFeet.above(PeakPhantomSpawnPolicy.HOVER_OFFSET), Blocks.STONE);
		Phantom phantom = context.spawn(EntityTypes.PHANTOM, relativeFeet.getX(), relativeFeet.getY(), relativeFeet.getZ(), EntitySpawnReason.NATURAL);
		context.assertTrue(!phantom.isAlive(), "phantom should be discarded when hover space is occupied");
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void hoverSpaceClearRequiresEmptyAir(GameTestHelper context) {
		BlockPos hover = context.absolutePos(new BlockPos(2, 8, 2));
		context.setBlock(new BlockPos(2, 8, 2), Blocks.AIR);
		context.assertTrue(PeakPhantomSpawnPolicy.isHoverSpaceClear(context.getLevel(), hover), "air should be a valid hover cell");
		context.setBlock(new BlockPos(2, 8, 2), Blocks.STONE);
		context.assertTrue(!PeakPhantomSpawnPolicy.isHoverSpaceClear(context.getLevel(), hover), "stone should occupy the hover cell");
		context.succeed();
	}

	private static void assertPhantomInPool(GameTestHelper context, ResourceKey<Biome> biome, boolean expected) {
		Holder<Biome> holder = context.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome);
		boolean present = PeakPhantomSpawnPolicy.monsterPoolContainsPhantom(holder);
		context.assertTrue(
				present == expected,
				biome.identifier() + " phantom monster-pool entry expected " + expected + " but was " + present
		);
	}
}
