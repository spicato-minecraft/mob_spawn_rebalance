package mob_spawn_rebalance.mixin;

import mob_spawn_rebalance.spawn.CreeperSpawnPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Above-ground creeper packs are kept half the time. The other half is replaced with another
 * monster from the same list before the pack size is sampled, so the attempt still fills.
 */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

	@Inject(method = "getRandomSpawnMobAt", at = @At("RETURN"), cancellable = true)
	private static void mob_spawn_rebalance$redirectSurfaceCreepers(
			ServerLevel level,
			StructureManager structureManager,
			ChunkGenerator chunkGenerator,
			MobCategory category,
			RandomSource random,
			BlockPos pos,
			CallbackInfoReturnable<Optional<MobSpawnSettings.SpawnerData>> cir
	) {
		Optional<MobSpawnSettings.SpawnerData> redirected = CreeperSpawnPolicy.redirectSurfaceCreeper(
				level,
				category,
				pos,
				random,
				cir.getReturnValue()
		);
		if (redirected != cir.getReturnValue()) {
			cir.setReturnValue(redirected);
		}
	}
}
