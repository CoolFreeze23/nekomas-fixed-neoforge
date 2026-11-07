package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.util.options.EntitySpawnOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class SpawnOptionServerLevelMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void blockDisabledSpawns(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (EntitySpawnOption.isDisabled(entity.getType())) {
            cir.setReturnValue(false);
        }
    }
}
