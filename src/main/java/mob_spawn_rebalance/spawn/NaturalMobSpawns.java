package mob_spawn_rebalance.spawn;

import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * 26.3 moved biome spawn lists from {@code Biome#getMobSettings()} onto
 * {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
 */
public final class NaturalMobSpawns {

	private NaturalMobSpawns() {
	}

	public static MobSpawnSettings settings(Holder<Biome> biome) {
		EnvironmentAttributeMap.Entry<MobSpawnSettings, ?> entry = biome.value()
				.getAttributes()
				.get(EnvironmentAttributes.NATURAL_MOB_SPAWNS);
		if (entry == null) {
			return EnvironmentAttributes.NATURAL_MOB_SPAWNS.defaultValue();
		}
		return entry.applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS.defaultValue());
	}

	public static WeightedList<MobSpawnSettings.SpawnerData> monsterPool(Holder<Biome> biome) {
		WeightedList<MobSpawnSettings.SpawnerData> mobs = settings(biome).getMobsInCategory(MobCategory.MONSTER);
		return mobs == null ? WeightedList.of() : mobs;
	}
}
