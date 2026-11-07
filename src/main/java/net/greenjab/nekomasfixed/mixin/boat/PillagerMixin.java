package net.greenjab.nekomasfixed.mixin.boat;

import net.minecraft.world.entity.monster.Pillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Pillager.class)
public class PillagerMixin {

    @ModifyArg(method = "registerGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raider$HoldGroundAttackGoal;<init>(Lnet/minecraft/world/entity/raid/Raider;Lnet/minecraft/world/entity/monster/AbstractIllager;F)V"), index = 2)
    private float shootFurther(float distance) {
        return 15;
    }

    @ModifyConstant(method = "registerGoals", constant = @Constant(floatValue = 8.0f, ordinal = 0))
    private float shootFurther2(float distance) {
        return 12;
    }
}
