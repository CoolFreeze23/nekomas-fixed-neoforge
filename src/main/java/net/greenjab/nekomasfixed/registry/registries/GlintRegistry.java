package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.item.PrismItem;
import net.greenjab.nekomasfixed.registry.other.ArmorGlint;
import net.greenjab.nekomasfixed.registry.other.GlintColor;
import net.greenjab.nekomasfixed.registry.recipe.ArmorGlintRecipe;
import net.greenjab.nekomasfixed.util.AllDyes;
import net.greenjab.nekomasfixed.util.TreasureGlints;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/** Armor glints: the components, the prisms that apply them and their smithing recipe. */
public class GlintRegistry {
    public static final DataComponentType<Holder<GlintColor>> PROVIDES_GLINT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, NekomasFixed.id("provides_glint"),
            DataComponentType.<Holder<GlintColor>>builder().persistent(GlintColor.CODEC).networkSynchronized(GlintColor.STREAM_CODEC).cacheEncoding().build());

    public static final DataComponentType<ArmorGlint> ARMOR_GLINT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, NekomasFixed.id("armor_glint"),
            DataComponentType.<ArmorGlint>builder().persistent(ArmorGlint.CODEC).networkSynchronized(ArmorGlint.STREAM_CODEC).cacheEncoding().build());

    public static final Item WHITE_PRISM = registerPrism(AllDyes.WHITE);
    public static final Item LIGHT_GRAY_PRISM = registerPrism(AllDyes.LIGHT_GRAY);
    public static final Item GRAY_PRISM = registerPrism(AllDyes.GRAY);
    public static final Item BLACK_PRISM = registerPrism(AllDyes.BLACK);
    public static final Item BROWN_PRISM = registerPrism(AllDyes.BROWN);
    public static final Item RED_PRISM = registerPrism(AllDyes.RED);
    public static final Item ORANGE_PRISM = registerPrism(AllDyes.ORANGE);
    public static final Item YELLOW_PRISM = registerPrism(AllDyes.YELLOW);
    public static final Item LIME_PRISM = registerPrism(AllDyes.LIME);
    public static final Item GREEN_PRISM = registerPrism(AllDyes.GREEN);
    public static final Item CYAN_PRISM = registerPrism(AllDyes.CYAN);
    public static final Item LIGHT_BLUE_PRISM = registerPrism(AllDyes.LIGHT_BLUE);
    public static final Item BLUE_PRISM = registerPrism(AllDyes.BLUE);
    public static final Item PURPLE_PRISM = registerPrism(AllDyes.PURPLE);
    public static final Item MAGENTA_PRISM = registerPrism(AllDyes.MAGENTA);
    public static final Item PINK_PRISM = registerPrism(AllDyes.PINK);
    public static final Item AMBER_PRISM = registerPrism(AllDyes.AMBER);
    public static final Item AQUA_PRISM = registerPrism(AllDyes.AQUA);
    public static final Item INDIGO_PRISM = registerPrism(AllDyes.INDIGO);
    public static final Item MAROON_PRISM = registerPrism(AllDyes.MAROON);

    public static final Item WATER_PRISM = registerTreasurePrism(TreasureGlints.WATER);
    public static final Item FIRE_PRISM = registerTreasurePrism(TreasureGlints.FIRE);
    public static final Item ENDER_PRISM = registerTreasurePrism(TreasureGlints.ENDER);
    public static final Item RAINBOW_PRISM = registerTreasurePrism(TreasureGlints.RAINBOW);
    public static final Item FROSTFIRE_PRISM = registerTreasurePrism(TreasureGlints.FROSTFIRE);
    public static final Item JUNGLE_PRISM = registerTreasurePrism(TreasureGlints.JUNGLE);
    public static final Item VOID_PRISM = registerTreasurePrism(TreasureGlints.VOID);
    public static final Item SKY_PRISM = registerTreasurePrism(TreasureGlints.SKY);
    public static final Item ANCIENT_PRISM = registerTreasurePrism(TreasureGlints.ANCIENT);
    public static final Item SCULK_PRISM = registerTreasurePrism(TreasureGlints.SCULK);

    private static Item registerPrism(AllDyes dye) {
        return registerPrism(dye.getSerializedName() + "_prism", GlintColorRegistry.GLINT_COLORS.get(dye), new Item.Properties());
    }

    private static Item registerTreasurePrism(TreasureGlints glint) {
        return registerPrism(glint.getSerializedName() + "_prism", GlintColorRegistry.TREASURE_GLINTS.get(glint), new Item.Properties().rarity(Rarity.EPIC));
    }

    private static Item registerPrism(String id, ResourceKey<GlintColor> glint, Item.Properties properties) {
        Item item = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id(id), new PrismItem(glint, properties));
        ItemGroupRegistry.COLOUR_EXTRAS.add(item);
        return item;
    }

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, NekomasFixed.id("armor_glint"), ArmorGlintRecipe.SERIALIZER);
    }
}
