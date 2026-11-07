package net.greenjab.nekomasfixed.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.greenjab.nekomasfixed.registry.item.GoatHornHelmetItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class GoatHornArmorLayerMixin {

    // The horned helmet draws in its own layer, here only its trim and glint would land on the plain helmet shape.
    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void skipGoatHornHelmets(PoseStack poseStack, MultiBufferSource buffer, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<?> model,
                                     float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch,
                                     CallbackInfo ci) {
        if (entity.getItemBySlot(slot).getItem() instanceof GoatHornHelmetItem) {
            ci.cancel();
        }
    }
}
