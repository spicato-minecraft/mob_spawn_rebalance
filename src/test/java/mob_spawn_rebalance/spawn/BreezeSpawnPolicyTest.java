package mob_spawn_rebalance.spawn;

import mob_spawn_rebalance.MinecraftTestBootstrap;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BreezeSpawnPolicyTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void peaksMatchPhantomHuntingGround() {
		assertEquals(PeakPhantomSpawnPolicy.PEAK_BIOMES, BreezeSpawnPolicy.PEAK_BIOMES);
		assertTrue(BreezeSpawnPolicy.isPeakBiomeKey(Biomes.JAGGED_PEAKS));
		assertTrue(BreezeSpawnPolicy.isPeakBiomeKey(Biomes.FROZEN_PEAKS));
		assertTrue(BreezeSpawnPolicy.isPeakBiomeKey(Biomes.STONY_PEAKS));
		assertFalse(BreezeSpawnPolicy.isPeakBiomeKey(Biomes.SNOWY_SLOPES));
		assertFalse(BreezeSpawnPolicy.isPeakBiomeKey(Biomes.WINDSWEPT_HILLS));
	}

	@Test
	void allThreeBadlandsCountAsMesa() {
		assertEquals(3, BreezeSpawnPolicy.MESA_BIOMES.size());
		assertTrue(BreezeSpawnPolicy.isMesaBiomeKey(Biomes.BADLANDS));
		assertTrue(BreezeSpawnPolicy.isMesaBiomeKey(Biomes.WOODED_BADLANDS));
		assertTrue(BreezeSpawnPolicy.isMesaBiomeKey(Biomes.ERODED_BADLANDS));
		assertFalse(BreezeSpawnPolicy.isMesaBiomeKey(Biomes.DESERT));
		assertFalse(BreezeSpawnPolicy.isMesaBiomeKey(Biomes.SAVANNA));
	}

	@Test
	void spawnWeightIsCommonNotATakeover() {
		assertEquals(200, BreezeSpawnPolicy.SPAWN_WEIGHT);
		assertEquals(1, BreezeSpawnPolicy.SPAWN_MIN_GROUP);
		assertEquals(2, BreezeSpawnPolicy.SPAWN_MAX_GROUP);
	}

	@Test
	void mesaPlateauStartsAtSeventy() {
		assertEquals(70, BreezeSpawnPolicy.MESA_PLATEAU_MIN_Y);
		assertFalse(BreezeSpawnPolicy.isMesaPlateauY(40));
		assertFalse(BreezeSpawnPolicy.isMesaPlateauY(63));
		assertFalse(BreezeSpawnPolicy.isMesaPlateauY(69));
		assertTrue(BreezeSpawnPolicy.isMesaPlateauY(70));
		assertTrue(BreezeSpawnPolicy.isMesaPlateauY(88));
		assertTrue(BreezeSpawnPolicy.isMesaPlateauY(120));
	}
}
