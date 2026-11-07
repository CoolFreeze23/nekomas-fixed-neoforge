package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.item.DyedBrushItem;
import net.greenjab.nekomasfixed.registry.other.DyedBrushBehaviour;
import net.greenjab.nekomasfixed.util.AllDyes;
import net.greenjab.nekomasfixed.util.ItemDyeMap;
import net.greenjab.nekomasfixed.util.RandomDyeModifier;
import net.greenjab.nekomasfixed.util.RandomFireworkModifier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

/** The dyed brushes in all 20 colours, and the loot functions that pick a random dye or firework. */
public class BrushRegistry {
    private static final AllDyes[] TAB_ORDER = {
            AllDyes.WHITE, AllDyes.LIGHT_GRAY, AllDyes.GRAY, AllDyes.BLACK, AllDyes.BROWN, AllDyes.RED, AllDyes.ORANGE, AllDyes.YELLOW,
            AllDyes.LIME, AllDyes.GREEN, AllDyes.CYAN, AllDyes.LIGHT_BLUE, AllDyes.BLUE, AllDyes.PURPLE, AllDyes.MAGENTA, AllDyes.PINK,
            AllDyes.AMBER, AllDyes.AQUA, AllDyes.INDIGO, AllDyes.MAROON};

    public static final LootItemFunctionType<RandomFireworkModifier> RANDOM_FIREWORK = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE,
            NekomasFixed.id("random_firework"), new LootItemFunctionType<>(RandomFireworkModifier.CODEC));
    public static final LootItemFunctionType<RandomDyeModifier> RANDOM_DYE = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE,
            NekomasFixed.id("random_dye"), new LootItemFunctionType<>(RandomDyeModifier.CODEC));

    public static void register() {
        for (AllDyes dye : TAB_ORDER) {
            Item brush = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id(dye.getSerializedName() + "_dyed_brush"),
                    new DyedBrushItem(dye, new Item.Properties().stacksTo(1).durability(64)));
            ItemDyeMap.BRUSH.put(dye, brush);
            ItemGroupRegistry.COLOUR_EXTRAS.add(brush);
            DispenserBlock.registerBehavior(brush, new DyedBrushBehaviour());
        }
    }
}
