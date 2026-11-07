package net.greenjab.nekomasfixed.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.mixin.client.ItemPropertiesAccessor;
import net.greenjab.nekomasfixed.registry.registries.WildfireRegistry;
import net.greenjab.nekomasfixed.render.entity.FireBombRenderer;
import net.greenjab.nekomasfixed.render.entity.ThrownWildfireTridentRenderer;
import net.greenjab.nekomasfixed.render.entity.WildfireRenderer;
import net.greenjab.nekomasfixed.render.entity.WildfireShieldModelRenderer;
import net.greenjab.nekomasfixed.render.entity.WildfireTridentModelRenderer;
import net.greenjab.nekomasfixed.render.entity.model.WildfireModel;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class WildfireClient {
    public static final ModelLayerLocation WILD_FIRE = new ModelLayerLocation(NekomasFixed.id("wildfire"), "main");
    public static final ModelLayerLocation WILDFIRE_TRIDENT = new ModelLayerLocation(NekomasFixed.id("wildfirefire_trident"), "main");

    public static void listen(IEventBus modBus) {
        modBus.addListener(WildfireClient::registerLayers);
        modBus.addListener(WildfireClient::registerRenderers);
        modBus.addListener(WildfireClient::registerItemExtensions);
        modBus.addListener(WildfireClient::onClientSetup);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WILD_FIRE, WildfireModel::getTexturedModelData);
        event.registerLayerDefinition(WILDFIRE_TRIDENT, TridentModel::createLayer);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WildfireRegistry.WILDFIRE, WildfireRenderer::new);
        event.registerEntityRenderer(WildfireRegistry.WILDFIRE_TRIDENT_ENTITY, ThrownWildfireTridentRenderer::new);
        event.registerEntityRenderer(WildfireRegistry.FIRE_BOMB, FireBombRenderer::new);
    }

    private static void registerItemExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new WildfireTridentModelRenderer();
                return renderer;
            }
        }, WildfireRegistry.WILDFIRE_TRIDENT);
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new WildfireShieldModelRenderer();
                return renderer;
            }
        }, WildfireRegistry.WILDFIRE_SHIELD);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClampedItemPropertyFunction usingItem = (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
            ItemPropertiesAccessor.invokeRegister(WildfireRegistry.WILDFIRE_SHIELD, ResourceLocation.withDefaultNamespace("blocking"), usingItem);
            ItemPropertiesAccessor.invokeRegister(WildfireRegistry.WILDFIRE_TRIDENT, ResourceLocation.withDefaultNamespace("throwing"), usingItem);
        });
    }
}
