package net.greenjab.nekomasfixed.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.greenjab.nekomasfixed.render.other.TintedGlint;
import net.greenjab.nekomasfixed.util.GlintContext;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerGlintMixin {
    private static final String RENDER_ARMOR_PIECE = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V";

    @Inject(method = RENDER_ARMOR_PIECE, at = @At("HEAD"))
    private void nekomasfixed$setGlint(PoseStack pose, MultiBufferSource buffer, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<?> model, float a, float b, float c, float d, float e, float f, CallbackInfo ci) {
        GlintContext.set(entity.getItemBySlot(slot));
    }

    @Inject(method = RENDER_ARMOR_PIECE, at = @At("RETURN"))
    private void nekomasfixed$clearGlint(PoseStack pose, MultiBufferSource buffer, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<?> model, float a, float b, float c, float d, float e, float f, CallbackInfo ci) {
        GlintContext.clear();
    }

    @ModifyExpressionValue(method = "renderGlint(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorEntityGlint()Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType nekomasfixed$tintArmorFoil(RenderType original, @Local(argsOnly = true) MultiBufferSource buffer) {
        return TintedGlint.armor(buffer, original);
    }
}
