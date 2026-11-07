package net.greenjab.nekomasfixed.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.other.AnimalTooltipData;
import net.greenjab.nekomasfixed.registry.registries.TermiteRegistry;
import net.greenjab.nekomasfixed.render.entity.TermiteRenderer;
import net.greenjab.nekomasfixed.render.entity.feature.FlowerCrownLayer;
import net.greenjab.nekomasfixed.render.entity.feature.GoatHornHelmetLayer;
import net.greenjab.nekomasfixed.render.entity.model.FlowerCrownModel;
import net.greenjab.nekomasfixed.render.entity.model.GoatHornHelmetModel;
import net.greenjab.nekomasfixed.render.entity.model.TermiteModel;
import net.greenjab.nekomasfixed.render.other.AnimalTooltipComponent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

public class TermiteCrownClient {

    public static final ModelLayerLocation TERMITE = new ModelLayerLocation(NekomasFixed.id("termite"), "main");
    public static final ModelLayerLocation FLOWER_CROWN = new ModelLayerLocation(NekomasFixed.id("flower_crown"), "main");
    public static final ModelLayerLocation GOAT_HORN_HELMET = new ModelLayerLocation(NekomasFixed.id("goat_horn_helmet"), "main");
    public static final ModelLayerLocation GOAT_HORN_HELMET_TRIM = new ModelLayerLocation(NekomasFixed.id("goat_horn_helmet"), "trim");

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TERMITE, TermiteModel::getTexturedModelData);
        event.registerLayerDefinition(FLOWER_CROWN, FlowerCrownModel::getTexturedModelData);
        event.registerLayerDefinition(GOAT_HORN_HELMET, GoatHornHelmetModel::getTexturedModelData);
        event.registerLayerDefinition(GOAT_HORN_HELMET_TRIM, GoatHornHelmetModel::getTexturedModelDataForTrim);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TermiteRegistry.TERMITE, TermiteRenderer::new);
    }

    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AnimalTooltipData.class, data -> new AnimalTooltipComponent(data.contents()));
    }

    /** Puts the crown and horned helmet layers on every humanoid renderer: players, armour stands, target dummies, mobs. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        EntityRendererProvider.Context context = event.getContext();
        for (PlayerSkin.Model skin : event.getSkins()) {
            addHumanoidLayers(event.getSkin(skin), context);
        }
        for (EntityType type : event.getEntityTypes()) {
            addHumanoidLayers((EntityRenderer) event.getRenderer(type), context);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addHumanoidLayers(EntityRenderer<?> renderer, EntityRendererProvider.Context context) {
        if (renderer instanceof LivingEntityRenderer living && living.getModel() instanceof HumanoidModel) {
            living.addLayer(new FlowerCrownLayer(living, context.getModelSet()));
            living.addLayer(new GoatHornHelmetLayer(living, context.getModelSet(), context.getModelManager()));
        }
    }
}
