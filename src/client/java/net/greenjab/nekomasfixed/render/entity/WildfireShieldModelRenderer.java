package net.greenjab.nekomasfixed.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WildfireShieldModelRenderer extends BlockEntityWithoutLevelRenderer {
	private final ShieldModel model;

	public static final ResourceLocation TEXTURE = NekomasFixed.id("textures/entity/wildfire_shield/default.png");
	public static final ResourceLocation TEXTURE_SOUL = NekomasFixed.id("textures/entity/wildfire_shield/soul.png");

	public WildfireShieldModelRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
		this.model = new ShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SHIELD));
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
		ResourceLocation texture = stack.getDamageValue() < stack.getMaxDamage() / 2 ? TEXTURE : TEXTURE_SOUL;
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffers, RenderType.entitySolid(texture), true, stack.hasFoil());
		this.model.renderToBuffer(poseStack, vertexConsumer, light, overlay);
		poseStack.popPose();
	}
}
