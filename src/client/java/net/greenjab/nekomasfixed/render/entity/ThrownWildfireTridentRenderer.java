package net.greenjab.nekomasfixed.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registries.WildfireClient;
import net.greenjab.nekomasfixed.registry.entity.WildfireTrident;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ThrownWildfireTridentRenderer extends EntityRenderer<WildfireTrident> {
	public static final ResourceLocation TEXTURE = NekomasFixed.id("textures/entity/wildfire_trident/default.png");
	private final TridentModel model;

	public ThrownWildfireTridentRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new TridentModel(context.bakeLayer(WildfireClient.WILDFIRE_TRIDENT));
	}

	@Override
	public void render(WildfireTrident entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 90.0F));
		VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffers, RenderType.entityCutout(TEXTURE), false, entity.isEnchanted());
		this.model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(WildfireTrident entity) {
		return TEXTURE;
	}
}
