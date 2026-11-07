package net.greenjab.nekomasfixed;

import net.greenjab.nekomasfixed.mixin.client.ItemPropertiesAccessor;
import net.greenjab.nekomasfixed.registries.KilnQuiverGlintClient;
import net.greenjab.nekomasfixed.registries.ModBlockEntityRendererRegistry;
import net.greenjab.nekomasfixed.registries.ModEntityRendererRegistry;
import net.greenjab.nekomasfixed.registry.registries.BlockRegistry;
import net.greenjab.nekomasfixed.registry.registries.ClamStateProperty;
import net.greenjab.nekomasfixed.registry.registries.ItemRegistry;
import net.greenjab.nekomasfixed.registry.registries.ScreenHandlerRegistry;
import net.greenjab.nekomasfixed.screen.PyrotechnicsTableScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.client.renderer.RenderType;

@Mod(value = NekomasFixed.NAMESPACE, dist = Dist.CLIENT)
public class NekomasFixedClient {

    public NekomasFixedClient(IEventBus modBus) {
        modBus.addListener(NekomasFixedClient::onClientSetup);
        modBus.addListener(NekomasFixedClient::onRegisterMenuScreens);
        modBus.addListener(NekomasFixedClient::onRegisterLayers);
        modBus.addListener(NekomasFixedClient::onRegisterRenderers);
        KilnQuiverGlintClient.register(modBus);
    }

    private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ScreenHandlerRegistry.PYROTECHNICS, PyrotechnicsTableScreen::new);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModEntityRendererRegistry.registerLayers(event);
        ModBlockEntityRendererRegistry.registerLayers(event);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModEntityRendererRegistry.registerRenderers(event);
        ModBlockEntityRendererRegistry.registerRenderers(event);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NekomasFixedClient::setup);
    }

    private static void setup() {
        Sheets.addWoodType(BlockRegistry.BAOBAB_WOODTYPE);
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BAOBAB_SAPLING, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BAOBAB_FRUIT, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.GHOST_PEPPER_SHRUB, RenderType.cutout());
        // Ancient stained glass + panes render translucent, like vanilla stained glass (else they
        // render solid because they're not in ItemBlockRenderTypes' per-block map).
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.AMBER_STAINED_GLASS, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.AQUA_STAINED_GLASS, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.INDIGO_STAINED_GLASS, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.MAROON_STAINED_GLASS, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.AMBER_STAINED_GLASS_PANE, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.AQUA_STAINED_GLASS_PANE, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.INDIGO_STAINED_GLASS_PANE, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.MAROON_STAINED_GLASS_PANE, RenderType.translucent());
        // Corrupted beacon has a stained-glass shell in its model; without this it renders
        // solid (opaque purple in hand) like the ancient glass did.
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.CORRUPTED_BEACON, RenderType.translucent());

        ClamStateProperty clamState = new ClamStateProperty();
        ItemPropertiesAccessor.invokeRegister(ItemRegistry.CLAM, net.greenjab.nekomasfixed.NekomasFixed.id("clam_state"), clamState);
        ItemPropertiesAccessor.invokeRegister(ItemRegistry.CLAM_BLUE, net.greenjab.nekomasfixed.NekomasFixed.id("clam_state"), clamState);
        ItemPropertiesAccessor.invokeRegister(ItemRegistry.CLAM_PINK, net.greenjab.nekomasfixed.NekomasFixed.id("clam_state"), clamState);
        ItemPropertiesAccessor.invokeRegister(ItemRegistry.CLAM_PURPLE, net.greenjab.nekomasfixed.NekomasFixed.id("clam_state"), clamState);
    }
}
