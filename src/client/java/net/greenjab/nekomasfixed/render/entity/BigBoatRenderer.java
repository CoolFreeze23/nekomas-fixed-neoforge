package net.greenjab.nekomasfixed.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.greenjab.nekomasfixed.registry.entity.BigBoat;
import net.greenjab.nekomasfixed.render.entity.model.BigBoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

public class BigBoatRenderer<T extends BigBoat> extends EntityRenderer<T> {
	private final ResourceLocation texture;
	public final BigBoatModel<T> model;
	protected final ItemRenderer itemRenderer;

	public BigBoatRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.shadowRadius = 0.8F;
		this.texture = layer.getModel().withPath(path -> "textures/entity/" + path + ".png");
		this.model = getThisModel(context, layer);
	}

	public BigBoatModel<T> getThisModel(EntityRendererProvider.Context context, ModelLayerLocation layer) {
		return new BigBoatModel<>(context.bakeLayer(layer));
	}

	@Override
	public void render(T boat, float entityYaw, float partialTick, PoseStack poseStack, @NonNull MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.375F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
		float wobbleTicks = (float) boat.getHurtTime() - partialTick;
		float wobbleStrength = Math.max(boat.getDamage() / 2.0F - partialTick, 0.0F);
		if (wobbleTicks > 0.0F) {
			poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(wobbleTicks) * wobbleTicks * wobbleStrength / 10.0F * (float) boat.getHurtDir()));
		}
		float bubbleWobble = boat.getBubbleAngle(partialTick);
		if (!boat.isUnderWater() && !Mth.equal(bubbleWobble, 0.0F)) {
			poseStack.mulPose(new Quaternionf().setAngleAxis(bubbleWobble * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
		}
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		this.model.setupAnim(boat, partialTick, 0.0F, 0.0F, 0.0F, 0.0F);
		VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.texture));
		this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		if (boat.getBanner().is(ItemTags.BANNERS)) {
			renderBanners(boat, poseStack, buffer, packedLight);
		}
		poseStack.popPose();
		super.render(boat, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	public void renderBanners(T boat, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.translate(0.0F, 1F, 0.125F);
		renderBanner(boat, poseStack, buffer, packedLight);
	}

	protected void renderBanner(T boat, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		this.itemRenderer.renderStatic(boat.getBanner(), ItemDisplayContext.HEAD, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, boat.level(), boat.getId());
	}

	@Override
	public @NonNull ResourceLocation getTextureLocation(@NonNull T boat) {
		return this.texture;
	}
}
