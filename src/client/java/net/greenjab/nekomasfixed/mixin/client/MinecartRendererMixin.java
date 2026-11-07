package net.greenjab.nekomasfixed.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.greenjab.nekomasfixed.render.entity.model.CustomMinecartModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecartRenderer.class)
public class MinecartRendererMixin {

    @Unique private static final ResourceLocation NEW_MINECART_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/minecart/new_minecart.png");

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/client/model/geom/ModelPart;)Lnet/minecraft/client/model/MinecartModel;"))
    private static MinecartModel<?> useCustomMinecartModel(ModelPart root, Operation<MinecartModel<?>> original) {
        return new CustomMinecartModel<>(root);
    }

    @ModifyExpressionValue(method = "getTextureLocation(Lnet/minecraft/world/entity/vehicle/AbstractMinecart;)Lnet/minecraft/resources/ResourceLocation;", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/MinecartRenderer;MINECART_LOCATION:Lnet/minecraft/resources/ResourceLocation;", opcode = Opcodes.GETSTATIC))
    private ResourceLocation useCustomMinecartTexture(ResourceLocation original) {
        return NEW_MINECART_LOCATION;
    }

    /** The wheels turn with the position and direction of the cart, which only the renderer knows between ticks. */
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/vehicle/AbstractMinecart;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    private void passWheelPose(EntityModel<Entity> model, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, Operation<Void> original,
                               @Local(argsOnly = true, ordinal = 0) float entityYaw, @Local(argsOnly = true, ordinal = 1) float partialTicks) {
        original.call(model, entity, partialTicks, entityYaw, ageInTicks, netHeadYaw, headPitch);
    }
}
