package mob_spawn_rebalance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModLoadsUnitTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void modMainClassLoads() {
		assertNotNull(MobSpawnRebalance.MOD_ID);
		assertEquals("mob_spawn_rebalance", MobSpawnRebalance.MOD_ID);
	}
}
