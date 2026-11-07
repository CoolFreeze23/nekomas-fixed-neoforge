package net.greenjab.nekomasfixed.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.mixin.client.ItemPropertiesAccessor;
import net.greenjab.nekomasfixed.registry.item.quiver.QuiverTooltip;
import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.greenjab.nekomasfixed.registry.registries.KilnRegistry;
import net.greenjab.nekomasfixed.render.entity.CustomArrowRenderer;
import net.greenjab.nekomasfixed.render.other.ClientQuiverTooltip;
import net.greenjab.nekomasfixed.render.other.TintedGlint;
import net.greenjab.nekomasfixed.render.special.TippedArrowTint;
import net.greenjab.nekomasfixed.screen.FletchingScreen;
import net.greenjab.nekomasfixed.screen.KilnScreen;
import net.greenjab.nekomasfixed.util.QuiverComponentChangeProperty;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client registration for the kiln, fletching table, quiver, tipped arrows and glints. */
public final class KilnQuiverGlintClient {
    private KilnQuiverGlintClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModRecipeBookCategories::register);
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(KilnRegistry.KILN_MENU, KilnScreen::new);
            event.register(KilnRegistry.FLETCHING_MENU, FletchingScreen::new);
        });
        modBus.addListener((RegisterClientTooltipComponentFactoriesEvent event) ->
                event.register(QuiverTooltip.class, tooltip -> new ClientQuiverTooltip(tooltip.contents())));
        modBus.addListener((RegisterColorHandlersEvent.Item event) ->
                event.register(new TippedArrowTint(), ArcheryRegistry.TIPPED_ARROW_CUSTOM));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(ArcheryRegistry.CUSTOM_ARROW, CustomArrowRenderer::new));
        modBus.addListener((RegisterClientReloadListenersEvent event) ->
                event.registerReloadListener((ResourceManagerReloadListener) manager -> TintedGlint.clearCache()));
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() ->
                ItemPropertiesAccessor.invokeRegister(ArcheryRegistry.QUIVER, NekomasFixed.id("empty"), new QuiverComponentChangeProperty())));
    }
}
