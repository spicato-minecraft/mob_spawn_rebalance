package mob_spawn_rebalance.mixin;

import mob_spawn_rebalance.spawn.PeakPhantomSpawnPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Natural phantoms are rolled on the outdoor surface, then lifted into empty air above it.
 */
@Mixin(Phantom.class)
public abstract class PhantomMixin {

	@Inject(method = "finalizeSpawn", at = @At("TAIL"))
	private void mob_spawn_rebalance$hoverAboveSurface(
			ServerLevelAccessor level,
			DifficultyInstance difficulty,
			EntitySpawnReason reason,
			SpawnGroupData spawnGroupData,
			CallbackInfoReturnable<SpawnGroupData> cir
	) {
		if (reason != EntitySpawnReason.NATURAL) {
			return;
		}
		Phantom phantom = (Phantom) (Object) this;
		BlockPos hover = PeakPhantomSpawnPolicy.hoverPos(phantom.blockPosition());
		if (!PeakPhantomSpawnPolicy.isHoverSpaceClear(level, hover)) {
			phantom.discard();
			return;
		}
		phantom.snapTo(hover.getX() + 0.5, hover.getY(), hover.getZ() + 0.5, phantom.getYRot(), phantom.getXRot());
	}
}
