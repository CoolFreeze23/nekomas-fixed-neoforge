package net.greenjab.nekomasfixed.render.entity;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registries.WildfireClient;
import net.greenjab.nekomasfixed.registry.entity.WildFire.WildfireEntity;
import net.greenjab.nekomasfixed.render.entity.model.WildfireModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class WildfireRenderer extends MobRenderer<WildfireEntity, WildfireModel> {
	private static final ResourceLocation TEXTURE = NekomasFixed.id("textures/entity/wildfire/default.png");
	private static final ResourceLocation TEXTURE_SOUL = NekomasFixed.id("textures/entity/wildfire/soul.png");

	public WildfireRenderer(EntityRendererProvider.Context context) {
		super(context, new WildfireModel(context.bakeLayer(WildfireClient.WILD_FIRE)), 0.5F);
	}

	@Override
	protected int getBlockLightLevel(WildfireEntity wildFireEntity, BlockPos blockPos) {
		return 15;
	}

	@Override
	public ResourceLocation getTextureLocation(WildfireEntity entity) {
		if (entity.isSoulActive()) return TEXTURE_SOUL;
		return TEXTURE;
	}
}
