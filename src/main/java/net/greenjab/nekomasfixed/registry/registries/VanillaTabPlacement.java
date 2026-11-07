package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Puts the mod's items next to their vanilla relatives in the vanilla creative tabs. The list holds, per item, the items it
 * follows; the first of those present in the tab decides the spot, so entries for newer vanilla content are skipped.
 */
public final class VanillaTabPlacement {
    private record Row(ResourceLocation item, List<ResourceLocation> anchors) {
    }

    private static Map<ResourceKey<CreativeModeTab>, List<Row>> rows;

    private VanillaTabPlacement() {
    }

    public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        List<Row> forTab = rows().get(event.getTabKey());
        if (forTab == null) return;
        for (Row row : forTab) {
            Item item = BuiltInRegistries.ITEM.get(row.item());
            if (item == Items.AIR) continue;
            ItemStack stack = new ItemStack(item);
            if (event.getParentEntries().contains(stack)) continue;
            ItemStack anchor = null;
            for (ResourceLocation id : row.anchors()) {
                Item candidate = BuiltInRegistries.ITEM.get(id);
                if (candidate != Items.AIR && event.getParentEntries().contains(new ItemStack(candidate))) {
                    anchor = new ItemStack(candidate);
                    break;
                }
            }
            if (anchor != null) {
                event.insertAfter(anchor, stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            } else {
                event.accept(stack);
            }
        }
    }

    private static Map<ResourceKey<CreativeModeTab>, List<Row>> rows() {
        if (rows != null) return rows;
        Map<ResourceKey<CreativeModeTab>, List<Row>> loaded = new HashMap<>();
        try (InputStream in = VanillaTabPlacement.class.getResourceAsStream("/assets/nekomasfixed/vanilla_tab_placement.txt")) {
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length != 3) continue;
                    ResourceKey<CreativeModeTab> tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                            ResourceLocation.withDefaultNamespace(parts[0].toLowerCase(Locale.ROOT)));
                    List<ResourceLocation> anchors = new ArrayList<>();
                    for (String id : parts[2].split(",")) {
                        if (!id.isEmpty()) anchors.add(ResourceLocation.parse(id));
                    }
                    loaded.computeIfAbsent(tab, key -> new ArrayList<>()).add(new Row(ResourceLocation.parse(parts[1]), anchors));
                }
            }
        } catch (IOException e) {
            NekomasFixed.LOGGER.error("Couldn't read the vanilla tab placement list", e);
        }
        rows = loaded;
        return rows;
    }
}
