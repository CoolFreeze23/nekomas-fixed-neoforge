package net.greenjab.nekomasfixed.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.greenjab.nekomasfixed.render.other.TintedGlint;
import net.greenjab.nekomasfixed.util.GlintContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererGlintMixin {

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"))
    private void nekomasfixed$setGlint(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack pose, MultiBufferSource buffer, int light, int overlay, BakedModel model, CallbackInfo ci) {
        GlintContext.set(stack);
    }

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("RETURN"))
    private void nekomasfixed$clearGlint(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack pose, MultiBufferSource buffer, int light, int overlay, BakedModel model, CallbackInfo ci) {
        GlintContext.clear();
    }

    @ModifyExpressionValue(method = {"getFoilBuffer", "getFoilBufferDirect", "getCompassFoilBuffer"}, at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;glint()Lnet/minecraft/client/renderer/RenderType;"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;glintTranslucent()Lnet/minecraft/client/renderer/RenderType;"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityGlint()Lnet/minecraft/client/renderer/RenderType;"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityGlintDirect()Lnet/minecraft/client/renderer/RenderType;")
    })
    private static RenderType nekomasfixed$tintFoil(RenderType original, @Local(argsOnly = true) MultiBufferSource buffer) {
        return TintedGlint.replace(original, buffer);
    }
}
