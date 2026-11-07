package net.greenjab.nekomasfixed.render.entity;

import net.greenjab.nekomasfixed.registry.entity.CustomArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CustomArrowRenderer extends ArrowRenderer<CustomArrow> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/tipped_arrow.png");

    public CustomArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomArrow entity) {
        return TEXTURE;
    }
}
