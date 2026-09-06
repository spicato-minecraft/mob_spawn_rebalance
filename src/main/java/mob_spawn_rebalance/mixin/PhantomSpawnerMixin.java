package mob_spawn_rebalance.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Peak phantoms use the normal monster spawn cycle. Vanilla insomnia spawns are disabled.
 */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void mob_spawn_rebalance$disableInsomniaSpawner(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
		ci.cancel();
	}
}
