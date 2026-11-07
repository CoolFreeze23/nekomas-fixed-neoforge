package net.greenjab.nekomasfixed.mixin.wildfire;

import net.greenjab.nekomasfixed.util.options.EntitySpawnOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void nekomasfixed$blockDisabledSpawns(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (EntitySpawnOption.isDisabled(entity.getType())) {
            cir.setReturnValue(false);
        }
    }
}
