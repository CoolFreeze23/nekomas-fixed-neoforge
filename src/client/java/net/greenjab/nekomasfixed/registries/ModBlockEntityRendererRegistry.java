package net.greenjab.nekomasfixed.registries;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.registries.BlockEntityTypeRegistry;
import net.greenjab.nekomasfixed.render.block.entity.*;
import net.greenjab.nekomasfixed.render.block.entity.model.ClamBlockModel;
import net.greenjab.nekomasfixed.render.block.entity.model.EndermanHeadBlockModel;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class ModBlockEntityRendererRegistry {

    public static final ModelLayerLocation CLAM =
            new ModelLayerLocation(NekomasFixed.id("clam"), "main");
    public static final ModelLayerLocation ENDERMAN_HEAD =
            new ModelLayerLocation(NekomasFixed.id("enderman_head"), "main");

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CLAM, ClamBlockModel::createBodyLayer);
        event.registerLayerDefinition(ENDERMAN_HEAD, EndermanHeadBlockModel::createBodyLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                BlockEntityTypeRegistry.CLAM_BLOCK_ENTITY, ClamBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                BlockEntityTypeRegistry.HOLLOW_LOG_BLOCK_ENTITY, HollowLogBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                BlockEntityTypeRegistry.ENDERMAN_HEAD_BLOCK_ENTITY, EndermanHeadBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                BlockEntityTypeRegistry.STACKED_CAKE_BLOCK_ENTITY, StackedCakeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                BlockEntityTypeRegistry.CLOCK_BLOCK_ENTITY, ClockBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                BlockEntityTypeRegistry.CORRUPTED_BEACON_BLOCK_ENTITY, CorruptedBeaconBlockEntityRenderer::new);
    }
}
