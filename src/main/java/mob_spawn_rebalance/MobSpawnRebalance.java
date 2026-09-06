package mob_spawn_rebalance;

import mob_spawn_rebalance.spawn.PeakPhantomSpawns;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MobSpawnRebalance implements ModInitializer {
	public static final String MOD_ID = "mob_spawn_rebalance";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PeakPhantomSpawns.register();
		LOGGER.info("Mob Spawn Rebalance initialized (peak phantoms)");
	}
}
