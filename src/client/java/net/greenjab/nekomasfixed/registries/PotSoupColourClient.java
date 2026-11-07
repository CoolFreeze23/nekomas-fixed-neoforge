package net.greenjab.nekomasfixed.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.cauldron.SoupCauldronBlock;
import net.greenjab.nekomasfixed.registry.block.entity.SoupCauldronBlockEntity;
import net.greenjab.nekomasfixed.registry.registries.PotRegistry;
import net.greenjab.nekomasfixed.registry.registries.SoupRegistry;
import net.greenjab.nekomasfixed.registry.registries.SulfurRegistry;
import net.greenjab.nekomasfixed.render.block.entity.SoupCauldronBlockEntityRenderer;
import net.greenjab.nekomasfixed.render.block.entity.TerracottaDecoratedPotBlockEntityRenderer;
import net.greenjab.nekomasfixed.render.block.item.TerracottaDecoratedPotItemRenderer;
import net.greenjab.nekomasfixed.render.other.NoxiousGasParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMaterialAtlasesEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Client registration for the terracotta pot, soup cauldron and sulfur blocks. */
public class PotSoupColourClient {

    public static void attach(IEventBus modBus) {
        modBus.addListener(PotSoupColourClient::onRegisterLayers);
        modBus.addListener(PotSoupColourClient::onRegisterRenderers);
        modBus.addListener(PotSoupColourClient::onRegisterAtlases);
        modBus.addListener(PotSoupColourClient::onRegisterItemExtensions);
        modBus.addListener(PotSoupColourClient::onRegisterBlockColours);
        modBus.addListener(PotSoupColourClient::onRegisterItemColours);
        modBus.addListener(PotSoupColourClient::onRegisterParticles);
        modBus.addListener(PotSoupColourClient::onClientSetup);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TerracottaDecoratedPotBlockEntityRenderer.CUSTOM_POT_SIDES, TerracottaDecoratedPotBlockEntityRenderer::createSidesLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(PotRegistry.TERRACOTTA_DECORATED_POT_BLOCK_ENTITY, TerracottaDecoratedPotBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(SoupRegistry.SOUP_CAULDRON_BLOCK_ENTITY, SoupCauldronBlockEntityRenderer::new);
    }

    private static void onRegisterAtlases(RegisterMaterialAtlasesEvent event) {
        event.register(TerracottaDecoratedPotBlockEntityRenderer.TERRACOTTA_POT, NekomasFixed.id("terracotta_decorated_pot"));
        event.register(TerracottaDecoratedPotBlockEntityRenderer.CUSTOM_POT, NekomasFixed.id("custom_pot"));
    }

    private static void onRegisterItemExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) this.renderer = new TerracottaDecoratedPotItemRenderer();
                return this.renderer;
            }
        }, PotRegistry.TERRACOTTA_DECORATED_POT_ITEM);
    }

    private static void onRegisterBlockColours(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> soupColour(level, pos), SoupRegistry.SOUP_CAULDRON);
    }

    /** Water colour, turning into the blended colour of the ingredients as the soup cooks. */
    private static int soupColour(BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) return -1;
        int water = BiomeColors.getAverageWaterColor(level, pos);
        if (level.getBlockEntity(pos) instanceof SoupCauldronBlockEntity soup) {
            float f = soup.getOpenNess(0);
            int s = SoupCauldronBlock.blendFoodColors(soup.getInputs());
            return FastColor.ARGB32.color(255,
                    (int) (f * (s >> 16 & 255) + (1 - f) * (water >> 16 & 255)),
                    (int) (f * (s >> 8 & 255) + (1 - f) * (water >> 8 & 255)),
                    (int) (f * (s & 255) + (1 - f) * (water & 255)));
        }
        return water;
    }

    private static void onRegisterItemColours(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 1 ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(stack, 0x385DC6)) : -1, SoupRegistry.SPECIAL_STEW);
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SulfurRegistry.NOXIOUS_GAS, NoxiousGasParticle.Provider::new);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(SoupRegistry.SOUP_CAULDRON, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(SulfurRegistry.SULFUR_FIRE, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SulfurRegistry.SULFUR_TORCH, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SulfurRegistry.WALL_SULFUR_TORCH, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SulfurRegistry.SULFUR_LANTERN, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SulfurRegistry.SULFUR_CAMPFIRE, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SulfurRegistry.GOLD_CHAIN, RenderType.cutoutMipped());
        });
    }
}
