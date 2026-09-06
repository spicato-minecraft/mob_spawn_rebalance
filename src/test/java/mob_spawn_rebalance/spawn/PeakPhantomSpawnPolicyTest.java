package mob_spawn_rebalance.spawn;

import mob_spawn_rebalance.MinecraftTestBootstrap;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakPhantomSpawnPolicyTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void onlyTheThreePeakBiomesQualify() {
		assertEquals(3, PeakPhantomSpawnPolicy.PEAK_BIOMES.size());
		assertTrue(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.JAGGED_PEAKS));
		assertTrue(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.FROZEN_PEAKS));
		assertTrue(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.STONY_PEAKS));
	}

	@Test
	void mountainShouldersAndHillsDoNotQualify() {
		assertFalse(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.SNOWY_SLOPES));
		assertFalse(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.GROVE));
		assertFalse(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.MEADOW));
		assertFalse(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.CHERRY_GROVE));
		assertFalse(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.WINDSWEPT_HILLS));
		assertFalse(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.WINDSWEPT_FOREST));
		assertFalse(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.WINDSWEPT_GRAVELLY_HILLS));
		assertFalse(PeakPhantomSpawnPolicy.isPeakBiomeKey(Biomes.PLAINS));
	}

	@Test
	void spawnWeightMatchesCreeperAndSkeleton() {
		assertEquals(100, PeakPhantomSpawnPolicy.SPAWN_WEIGHT);
		assertEquals(4, PeakPhantomSpawnPolicy.SPAWN_MIN_GROUP);
		assertEquals(4, PeakPhantomSpawnPolicy.SPAWN_MAX_GROUP);
	}
}
