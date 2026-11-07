package net.greenjab.nekomasfixed.mixin.client;

import net.greenjab.nekomasfixed.registry.registries.ItemRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws a bow or slingshot back with the pulling arm following the charge. */
@Mixin(HumanoidModel.class)
public abstract class PlayerAimMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;
    @Shadow public HumanoidModel.ArmPose rightArmPose;
    @Shadow public HumanoidModel.ArmPose leftArmPose;

    @Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
    private void rightArmDraw(T entity, CallbackInfo ci) {
        if (rightArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW) {
            nekomasfixed$charge(entity, true);
            ci.cancel();
        }
    }

    @Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
    private void leftArmDraw(T entity, CallbackInfo ci) {
        if (leftArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW) {
            nekomasfixed$charge(entity, false);
            ci.cancel();
        }
    }

    @Unique
    private void nekomasfixed$charge(T entity, boolean right) {
        boolean slingshot = entity.getItemBySlot(right == (entity.getMainArm() == HumanoidArm.RIGHT)
                ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND).is(ItemRegistry.SLINGSHOT);
        ModelPart holding = right ? rightArm : leftArm;
        ModelPart pulling = right ? leftArm : rightArm;
        holding.xRot = Mth.clamp(head.xRot, -1.2F, 1.2F) - 1.4835298F;
        holding.yRot = 0.7F * head.yRot - (float) (Math.PI / 12) * (right ? 1 : -1);
        pulling.xRot = holding.xRot + (slingshot ? -0.7F : 0);
        float pullTime = CrossbowItem.getChargeDuration(entity.getUseItem(), entity);
        float progress = Mth.clamp((float) entity.getTicksUsingItem(), 0.0F, pullTime) / pullTime;
        pulling.yRot = (float) (-Math.sin(pulling.xRot) * Mth.lerp(progress, 0.4F, 0.85F) * (right ? 1 : -1));
        pulling.zRot = (float) (Math.cos(pulling.xRot) * Mth.lerp(progress, 0.4F, 0.85F) * (right ? 1 : -1));
    }
}
