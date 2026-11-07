package net.greenjab.nekomasfixed.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.greenjab.nekomasfixed.registry.registries.ItemRegistry;

import java.util.EnumMap;

public class ItemDyeMap {
    public static final EnumMap<AllDyes, Item> DYE = new EnumMap<>(AllDyes.class);
    // Filled when the brushes register, see BrushRegistry.
    public static final EnumMap<AllDyes, Item> BRUSH = new EnumMap<>(AllDyes.class);

    static {
        DYE.put(AllDyes.WHITE, Items.WHITE_DYE);
        DYE.put(AllDyes.ORANGE, Items.ORANGE_DYE);
        DYE.put(AllDyes.MAGENTA, Items.MAGENTA_DYE);
        DYE.put(AllDyes.LIGHT_BLUE, Items.LIGHT_BLUE_DYE);
        DYE.put(AllDyes.YELLOW, Items.YELLOW_DYE);
        DYE.put(AllDyes.LIME, Items.LIME_DYE);
        DYE.put(AllDyes.PINK, Items.PINK_DYE);
        DYE.put(AllDyes.GRAY, Items.GRAY_DYE);
        DYE.put(AllDyes.LIGHT_GRAY, Items.LIGHT_GRAY_DYE);
        DYE.put(AllDyes.CYAN, Items.CYAN_DYE);
        DYE.put(AllDyes.PURPLE, Items.PURPLE_DYE);
        DYE.put(AllDyes.BLUE, Items.BLUE_DYE);
        DYE.put(AllDyes.BROWN, Items.BROWN_DYE);
        DYE.put(AllDyes.GREEN, Items.GREEN_DYE);
        DYE.put(AllDyes.RED, Items.RED_DYE);
        DYE.put(AllDyes.BLACK, Items.BLACK_DYE);
        DYE.put(AllDyes.AMBER, ItemRegistry.AMBER_DYE);
        DYE.put(AllDyes.AQUA, ItemRegistry.AQUA_DYE);
        DYE.put(AllDyes.INDIGO, ItemRegistry.INDIGO_DYE);
        DYE.put(AllDyes.MAROON, ItemRegistry.MAROON_DYE);
    }
}
