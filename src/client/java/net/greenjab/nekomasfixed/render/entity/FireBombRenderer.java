package net.greenjab.nekomasfixed.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.greenjab.nekomasfixed.registry.entity.WildFire.FireBomb;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class FireBombRenderer extends EntityRenderer<FireBomb> {
	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/item/blaze_powder.png");
	private static final RenderType LAYER = RenderType.entityTranslucentEmissive(TEXTURE);

	public FireBombRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected int getBlockLightLevel(FireBomb fireBombEntity, BlockPos blockPos) {
		return 15;
	}

	@Override
	public void render(FireBomb entity, float entityYaw, float partialTick, PoseStack matrices, MultiBufferSource buffers, int light) {
		matrices.pushPose();
		matrices.mulPose(this.entityRenderDispatcher.cameraOrientation());
		PoseStack.Pose pose = matrices.last();
		VertexConsumer vertexConsumer = buffers.getBuffer(LAYER);
		produceVertex(vertexConsumer, pose, light, 0.0F, 0, 0, 1);
		produceVertex(vertexConsumer, pose, light, 1.0F, 0, 1, 1);
		produceVertex(vertexConsumer, pose, light, 1.0F, 1, 1, 0);
		produceVertex(vertexConsumer, pose, light, 0.0F, 1, 0, 0);
		matrices.popPose();
		super.render(entity, entityYaw, partialTick, matrices, buffers, light);
	}

	public static void produceVertex(VertexConsumer vertexConsumer, PoseStack.Pose matrix, int light, float x, int z, int textureU, int textureV) {
		vertexConsumer.addVertex(matrix, x - 0.5F, z - 0.25F, 0.0F)
				.setColor(-1)
				.setUv(textureU, textureV)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(matrix, 0.0F, 1.0F, 0.0F);
	}

	@Override
	public ResourceLocation getTextureLocation(FireBomb entity) {
		return TEXTURE;
	}
}
