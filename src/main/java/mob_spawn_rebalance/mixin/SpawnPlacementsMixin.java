package mob_spawn_rebalance.mixin;

import mob_spawn_rebalance.spawn.BreezeSpawnPolicy;
import mob_spawn_rebalance.spawn.PeakPhantomSpawnPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Natural phantom attempts use ground placement so the night cycle finds the outdoor surface,
 * then spawn rules require sky + world-surface height (not caves) and an empty hover cell.
 * Natural breezes stay on outdoor peak/mesa surfaces; mesa also requires plateau Y.
 */
@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin {

	@Inject(method = "isSpawnPositionOk", at = @At("HEAD"), cancellable = true)
	private static void mob_spawn_rebalance$phantomOnGround(
			EntityType<?> type,
			LevelReader level,
			BlockPos pos,
			CallbackInfoReturnable<Boolean> cir
	) {
		if (type == EntityTypes.PHANTOM) {
			cir.setReturnValue(SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type));
		}
	}

	@Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
	private static void mob_spawn_rebalance$phantomSurfaceHoverRules(
			EntityType<?> type,
			ServerLevelAccessor level,
			EntitySpawnReason reason,
			BlockPos pos,
			RandomSource random,
			CallbackInfoReturnable<Boolean> cir
	) {
		if (type == EntityTypes.PHANTOM && reason == EntitySpawnReason.NATURAL) {
			boolean allowed = Monster.checkSurfaceMonstersSpawnRules(EntityTypes.PHANTOM, level, reason, pos, random)
					&& PeakPhantomSpawnPolicy.isOutdoorSurface(level, pos)
					&& PeakPhantomSpawnPolicy.isHoverSpaceClear(level, PeakPhantomSpawnPolicy.hoverPos(pos));
			cir.setReturnValue(allowed);
			return;
		}
		if (type == EntityTypes.BREEZE && reason == EntitySpawnReason.NATURAL) {
			boolean allowed = Monster.checkMonsterSpawnRules(EntityTypes.BREEZE, level, reason, pos, random)
					&& BreezeSpawnPolicy.allowsNaturalSpawnLocation(level, pos);
			cir.setReturnValue(allowed);
		}
	}
}
