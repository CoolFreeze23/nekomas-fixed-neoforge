package net.greenjab.nekomasfixed.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.greenjab.nekomasfixed.registries.WildfireClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WildfireTridentModelRenderer extends BlockEntityWithoutLevelRenderer {
	private final TridentModel model;

	public WildfireTridentModelRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
		this.model = new TridentModel(Minecraft.getInstance().getEntityModels().bakeLayer(WildfireClient.WILDFIRE_TRIDENT));
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffers, RenderType.entityCutout(ThrownWildfireTridentRenderer.TEXTURE), false, stack.hasFoil());
		this.model.renderToBuffer(poseStack, vertexConsumer, light, overlay);
		poseStack.popPose();
	}
}
