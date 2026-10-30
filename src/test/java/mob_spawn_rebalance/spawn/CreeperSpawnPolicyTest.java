package mob_spawn_rebalance.spawn;

import mob_spawn_rebalance.MinecraftTestBootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreeperSpawnPolicyTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void surfaceKeepsHalfOfPacks() {
		assertEquals(1, CreeperSpawnPolicy.SURFACE_KEEP_NUMERATOR);
		assertEquals(2, CreeperSpawnPolicy.SURFACE_KEEP_DENOMINATOR);
		for (int roll = 0; roll < CreeperSpawnPolicy.SURFACE_KEEP_DENOMINATOR; roll++) {
			ScriptedRandom random = new ScriptedRandom(roll);
			boolean allowed = CreeperSpawnPolicy.allowsNaturalAttempt(true, random);
			assertEquals(roll < CreeperSpawnPolicy.SURFACE_KEEP_NUMERATOR, allowed, "roll " + roll);
			assertEquals(List.of(CreeperSpawnPolicy.SURFACE_KEEP_DENOMINATOR), random.bounds);
		}
	}

	@Test
	void replacementUsesTheNextHostileWeight() {
		List<Weighted<MobSpawnSettings.SpawnerData>> pool = List.of(
				entry(EntityTypes.CREEPER, 100),
				entry(EntityTypes.ZOMBIE, 95),
				entry(EntityTypes.SKELETON, 100),
				entry(EntityTypes.SLIME, 100)
		);
		Predicate<EntityType<?>> surface = type -> type != EntityTypes.CREEPER && type != EntityTypes.SLIME;
		ScriptedRandom first = new ScriptedRandom(0);
		assertEquals(EntityTypes.ZOMBIE, CreeperSpawnPolicy.pickReplacement(pool, surface, first).orElseThrow().type());
		assertEquals(List.of(195), first.bounds);
		ScriptedRandom skeletonRoll = new ScriptedRandom(95);
		assertEquals(EntityTypes.SKELETON, CreeperSpawnPolicy.pickReplacement(pool, surface, skeletonRoll).orElseThrow().type());
	}

	@Test
	void replacementIsEmptyWhenNothingElseCanSpawn() {
		List<Weighted<MobSpawnSettings.SpawnerData>> pool = List.of(entry(EntityTypes.CREEPER, 100));
		ScriptedRandom random = new ScriptedRandom(0);
		assertTrue(CreeperSpawnPolicy.pickReplacement(pool, type -> type != EntityTypes.CREEPER, random).isEmpty());
		assertEquals(List.of(), random.bounds);
	}

	private static Weighted<MobSpawnSettings.SpawnerData> entry(EntityType<?> type, int weight) {
		return new Weighted<>(new MobSpawnSettings.SpawnerData(type, ConstantInt.of(4)), weight);
	}

	@Test
	void cavesDoNotDrawARoll() {
		ScriptedRandom random = new ScriptedRandom(0);
		assertTrue(CreeperSpawnPolicy.allowsNaturalAttempt(false, random));
		assertEquals(List.of(), random.bounds);
	}

	/** Returns scripted values from {@link #nextInt(int)} and records each bound. */
	private static final class ScriptedRandom implements RandomSource {
		private final int[] values;
		private int index;
		private final List<Integer> bounds = new ArrayList<>();

		private ScriptedRandom(int... values) {
			this.values = values;
		}

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
			return 0;
		}

		@Override
		public int nextInt(int bound) {
			bounds.add(bound);
			int value = values[index++];
			if (value < 0 || value >= bound) {
				throw new IllegalStateException("scripted " + value + " is outside " + bound);
			}
			return value;
		}

		@Override
		public long nextLong() {
			return 0L;
		}

		@Override
		public boolean nextBoolean() {
			return false;
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
