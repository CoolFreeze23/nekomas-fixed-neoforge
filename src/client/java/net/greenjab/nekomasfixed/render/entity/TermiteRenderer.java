package net.greenjab.nekomasfixed.render.entity;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registries.TermiteCrownClient;
import net.greenjab.nekomasfixed.registry.entity.Termite;
import net.greenjab.nekomasfixed.render.entity.model.TermiteModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.NonNull;

@OnlyIn(Dist.CLIENT)
public class TermiteRenderer extends MobRenderer<Termite, TermiteModel> {
    private static final ResourceLocation TEXTURE = NekomasFixed.id("textures/entity/termite/termite.png");

    public TermiteRenderer(EntityRendererProvider.Context context) {
        super(context, new TermiteModel(context.bakeLayer(TermiteCrownClient.TERMITE)), 0.25f);
    }

    @Override
    public @NonNull ResourceLocation getTextureLocation(@NonNull Termite entity) {
        return TEXTURE;
    }
}
