package mob_spawn_rebalance.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Loads every server mixin target so a renamed class fails here instead of in a player session.
 */
public class ServerMixinApplySweep {

	/**
	 * Binary names of the {@code @Mixin} targets in PhantomSpawnerMixin, PhantomMixin, and SpawnPlacementsMixin.
	 */
	private static final String[] MIXIN_TARGETS = {
			"net.minecraft.world.level.levelgen.PhantomSpawner",
			"net.minecraft.world.entity.monster.Phantom",
			"net.minecraft.world.entity.SpawnPlacements"
	};

	@GameTest(maxTicks = 20)
	public void loadsMixinTargets(GameTestHelper context) {
		for (String name : MIXIN_TARGETS) {
			try {
				Class.forName(name);
			} catch (ClassNotFoundException e) {
				context.fail("Mixin target failed to load: " + name);
			}
		}
		context.succeed();
	}
}
