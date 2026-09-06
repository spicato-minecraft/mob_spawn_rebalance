package mob_spawn_rebalance.gametest;

import mob_spawn_rebalance.spawn.PeakPhantomSpawnPolicy;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

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
	public void peakPhantomEntryMatchesCreeperGroupSize(GameTestHelper context) {
		Holder<Biome> biome = context.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.JAGGED_PEAKS);
		MobSpawnSettings.SpawnerData phantom = biome.value()
				.getMobSettings()
				.getMobs(MobCategory.MONSTER)
				.unwrap()
				.stream()
				.map(weighted -> weighted.value())
				.filter(data -> data.type() == EntityType.PHANTOM)
				.findFirst()
				.orElse(null);
		context.assertTrue(phantom != null, "jagged_peaks monster pool should include phantom");
		context.assertTrue(
				phantom.minCount() == PeakPhantomSpawnPolicy.SPAWN_MIN_GROUP
						&& phantom.maxCount() == PeakPhantomSpawnPolicy.SPAWN_MAX_GROUP,
				"phantom group size should match creeper/skeleton (4-4)"
		);
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
