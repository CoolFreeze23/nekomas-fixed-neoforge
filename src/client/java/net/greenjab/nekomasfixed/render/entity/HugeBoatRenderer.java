package net.greenjab.nekomasfixed.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.greenjab.nekomasfixed.registry.entity.HugeBoat;
import net.greenjab.nekomasfixed.render.entity.model.BigBoatModel;
import net.greenjab.nekomasfixed.render.entity.model.HugeBoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class HugeBoatRenderer extends BigBoatRenderer<HugeBoat> {

	public HugeBoatRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer) {
		super(context, layer);
	}

	@Override
	public void renderBanners(HugeBoat boat, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.translate(0.0F, 2F, -0.06F);
		renderBanner(boat, poseStack, buffer, packedLight);

		poseStack.translate(0.0F, -1.15F, -1.44F);
		renderBanner(boat, poseStack, buffer, packedLight);
	}

	@Override
	public BigBoatModel<HugeBoat> getThisModel(EntityRendererProvider.Context context, ModelLayerLocation layer) {
		return new HugeBoatModel<>(context.bakeLayer(layer));
	}
}
