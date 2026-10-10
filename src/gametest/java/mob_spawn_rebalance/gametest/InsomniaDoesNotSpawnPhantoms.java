package mob_spawn_rebalance.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;

/**
 * Vanilla insomnia spawning is disabled. A player who has been awake long enough that
 * {@link PhantomSpawner} would summon phantoms must still see none after that spawner ticks.
 */
public class InsomniaDoesNotSpawnPhantoms {

	/**
	 * Midnight after the regional-difficulty clock has saturated, so a hard world is always
	 * "harder than" the spawner's random roll and the sky is dark enough to spawn.
	 */
	private static final long NIGHT_WITH_SATURATED_DIFFICULTY = 1_530_000L;

	/** {@code nextInt(timeSinceRest) >= 72000} in PhantomSpawner. */
	private static final int VANILLA_INSOMNIA_THRESHOLD = 72000;

	private static final int SPAWN_ATTEMPTS = 8;

	@GameTest(maxTicks = 100, skyAccess = true)
	@SuppressWarnings("removal")
	public void insomniaDoesNotSpawnPhantoms(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		level.getServer().setDifficulty(Difficulty.HARD, true);
		level.getGameRules().set(GameRules.SPAWN_PHANTOMS, true, level.getServer());
		setNight(level);

		BlockPos anchor = context.absolutePos(new BlockPos(2, 0, 2));
		int x = anchor.getX();
		int z = anchor.getZ();
		int feetY = Math.max(level.getSeaLevel(), 63) + 2;
		loadSpawnChunks(level, x, z);
		clearSpawnVolume(level, x, z, feetY);

		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.teleportTo(level, x + 0.5, feetY, z + 0.5, Set.of(), 0.0F, 0.0F, true);
		Stat<Identifier> timeSinceRest = Stats.CUSTOM.get(Stats.TIME_SINCE_REST);
		player.getStats().setValue(player, timeSinceRest, Integer.MAX_VALUE);

		BlockPos feet = player.blockPosition();
		context.assertTrue(level.players().contains(player), "insomnia player should be in the level player list");
		context.assertTrue(!player.isSpectator(), "insomnia player should not be a spectator");
		context.assertTrue(feet.getY() >= level.getSeaLevel(), "insomnia player should be at or above sea level");
		context.assertTrue(level.canSeeSky(feet), "insomnia player should see the sky at " + feet);
		context.assertTrue(level.getSkyDarken() >= 5, "sky should be dark enough for vanilla phantoms, darken=" + level.getSkyDarken());
		context.assertTrue(
				player.getStats().getValue(timeSinceRest) > VANILLA_INSOMNIA_THRESHOLD,
				"time since rest should be high enough that vanilla can roll a phantom"
		);
		DifficultyInstance difficulty = level.getCurrentDifficultyAt(feet);
		context.assertTrue(
				difficulty.getEffectiveDifficulty() >= 3.0F,
				"regional difficulty should force the vanilla phantom roll, was " + difficulty.getEffectiveDifficulty()
		);

		AABB search = new AABB(x - 48, feetY - 8, z - 48, x + 49, feetY + 80, z + 49);
		for (Phantom existing : level.getEntities(EntityTypes.PHANTOM, search, phantom -> true)) {
			existing.discard();
		}

		for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
			new PhantomSpawner().tick(level, true);
		}

		List<Phantom> phantoms = level.getEntities(EntityTypes.PHANTOM, search, Phantom::isAlive);
		context.assertTrue(
				phantoms.isEmpty(),
				"insomnia should not spawn phantoms, found " + phantoms.size()
		);
		context.succeed();
	}

	private static void setNight(ServerLevel level) {
		Holder<WorldClock> overworld = level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD);
		level.clockManager().setTotalTicks(overworld, NIGHT_WITH_SATURATED_DIFFICULTY);
		level.updateSkyBrightness();
	}

	private static void loadSpawnChunks(ServerLevel level, int x, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				level.getChunk(chunkX + dx, chunkZ + dz);
			}
		}
	}

	/**
	 * Phantom attempts land 20–34 blocks above the player, ±10 on X/Z, then hover 20 higher.
	 * That whole box has to be empty air or a removed cancel would still not leave a phantom.
	 */
	private static void clearSpawnVolume(ServerLevel level, int x, int z, int feetY) {
		for (int dx = -11; dx <= 11; dx++) {
			for (int dz = -11; dz <= 11; dz++) {
				int columnX = x + dx;
				int columnZ = z + dz;
				int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, columnX, columnZ);
				int top = Math.min(Math.max(surface, feetY + 56), level.getMaxY());
				for (int y = feetY; y < top; y++) {
					BlockPos pos = new BlockPos(columnX, y, columnZ);
					if (!level.getBlockState(pos).isAir()) {
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
					}
				}
			}
		}
	}
}
