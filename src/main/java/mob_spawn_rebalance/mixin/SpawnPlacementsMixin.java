package mob_spawn_rebalance.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
 * Natural phantom attempts use ground + surface-monster rules (darkness and sky), like other peak hostiles.
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
		if (type == EntityType.PHANTOM) {
			cir.setReturnValue(SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type));
		}
	}

	@Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
	private static void mob_spawn_rebalance$phantomSurfaceRules(
			EntityType<?> type,
			ServerLevelAccessor level,
			EntitySpawnReason reason,
			BlockPos pos,
			RandomSource random,
			CallbackInfoReturnable<Boolean> cir
	) {
		if (type == EntityType.PHANTOM) {
			cir.setReturnValue(Monster.checkSurfaceMonstersSpawnRules(EntityType.PHANTOM, level, reason, pos, random));
		}
	}
}
