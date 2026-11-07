package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.KilnBlock;
import net.greenjab.nekomasfixed.registry.block.entity.KilnBlockEntity;
import net.greenjab.nekomasfixed.registry.recipe.KilnRecipe;
import net.greenjab.nekomasfixed.screen.FletchingMenu;
import net.greenjab.nekomasfixed.screen.KilnMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/** The kiln's block, block entity, menu and recipe type, and the fletching table's menu. */
public class KilnRegistry {
    public static final Block KILN = Registry.register(BuiltInRegistries.BLOCK, NekomasFixed.id("kiln"),
            new KilnBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).instrument(NoteBlockInstrument.BASEDRUM)
                    .sound(SoundType.GILDED_BLACKSTONE).requiresCorrectToolForDrops().strength(3.5f)));

    public static final Item KILN_ITEM = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("kiln"), new BlockItem(KILN, new Item.Properties()));

    public static final BlockEntityType<KilnBlockEntity> KILN_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, NekomasFixed.id("kiln"),
            BlockEntityType.Builder.of(KilnBlockEntity::new, KILN).build(null));

    public static final RecipeType<KilnRecipe> KILN_RECIPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, NekomasFixed.id("kiln"), new RecipeType<>() {
        @Override
        public String toString() {
            return "nekomasfixed:kiln";
        }
    });

    public static final MenuType<KilnMenu> KILN_MENU = Registry.register(BuiltInRegistries.MENU, NekomasFixed.id("kiln"),
            new MenuType<>(KilnMenu::new, FeatureFlags.VANILLA_SET));

    public static final MenuType<FletchingMenu> FLETCHING_MENU = Registry.register(BuiltInRegistries.MENU, NekomasFixed.id("fletching"),
            new MenuType<>(FletchingMenu::new, FeatureFlags.VANILLA_SET));

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, NekomasFixed.id("kilning"), KilnRecipe.SERIALIZER);
        ItemGroupRegistry.MAIN_EXTRAS.add(KILN_ITEM);
    }
}
