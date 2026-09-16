package mob_spawn_rebalance.gametest;

import mob_spawn_rebalance.spawn.BreezeSpawnPolicy;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;

public class BreezeSpawnGameTest {

	@GameTest(maxTicks = 100)
	public void peakAndMesaPoolsIncludeBreezes(GameTestHelper context) {
		assertBreezeInPool(context, Biomes.JAGGED_PEAKS, true);
		assertBreezeInPool(context, Biomes.FROZEN_PEAKS, true);
		assertBreezeInPool(context, Biomes.STONY_PEAKS, true);
		assertBreezeInPool(context, Biomes.BADLANDS, true);
		assertBreezeInPool(context, Biomes.WOODED_BADLANDS, true);
		assertBreezeInPool(context, Biomes.ERODED_BADLANDS, true);
		assertBreezeInPool(context, Biomes.SNOWY_SLOPES, false);
		assertBreezeInPool(context, Biomes.DESERT, false);
		assertBreezeInPool(context, Biomes.SAVANNA, false);
		assertBreezeInPool(context, Biomes.PLAINS, false);
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void naturalBreezeDeniedInPlainsEvenOnSurface(GameTestHelper context) {
		context.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
		var feet = context.absolutePos(new BlockPos(1, 1, 1));
		context.assertTrue(
				!BreezeSpawnPolicy.allowsNaturalSpawnLocation(context.getLevel(), feet),
				"plains outdoor surface should not allow natural breezes"
		);
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void breezeEntryMatchesCommonRates(GameTestHelper context) {
		assertEntry(context, Biomes.STONY_PEAKS);
		assertEntry(context, Biomes.BADLANDS);
		context.succeed();
	}

	private static void assertEntry(GameTestHelper context, ResourceKey<Biome> biomeKey) {
		Holder<Biome> biome = context.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);
		var entry = biome.value()
				.getMobSettings()
				.getMobs(MobCategory.MONSTER)
				.unwrap()
				.stream()
				.filter(weighted -> weighted.value().type() == EntityType.BREEZE)
				.findFirst()
				.orElse(null);
		context.assertTrue(entry != null, biomeKey.identifier() + " monster pool should include breeze");
		MobSpawnSettings.SpawnerData breeze = entry.value();
		context.assertTrue(
				entry.weight() == BreezeSpawnPolicy.SPAWN_WEIGHT,
				biomeKey.identifier() + " breeze weight should be " + BreezeSpawnPolicy.SPAWN_WEIGHT
		);
		context.assertTrue(
				breeze.minCount() == BreezeSpawnPolicy.SPAWN_MIN_GROUP
						&& breeze.maxCount() == BreezeSpawnPolicy.SPAWN_MAX_GROUP,
				biomeKey.identifier() + " breeze group size should be 1-2"
		);
	}

	private static void assertBreezeInPool(GameTestHelper context, ResourceKey<Biome> biome, boolean expected) {
		Holder<Biome> holder = context.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome);
		boolean present = BreezeSpawnPolicy.monsterPoolContainsBreeze(holder);
		context.assertTrue(
				present == expected,
				biome.identifier() + " breeze monster-pool entry expected " + expected + " but was " + present
		);
	}
}
