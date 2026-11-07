package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.cauldron.SoupCauldronBlock;
import net.greenjab.nekomasfixed.registry.block.entity.SoupCauldronBlockEntity;
import net.greenjab.nekomasfixed.registry.item.SpecialSoupItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SoupRegistry {
    public static final Block SOUP_CAULDRON = Registry.register(BuiltInRegistries.BLOCK, NekomasFixed.id("soup_cauldron"),
            new SoupCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)));
    public static final BlockEntityType<SoupCauldronBlockEntity> SOUP_CAULDRON_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, NekomasFixed.id("soup_cauldron"),
            BlockEntityType.Builder.of(SoupCauldronBlockEntity::new, SOUP_CAULDRON).build(null));
    public static final Item SPECIAL_STEW = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("special_stew"),
            new SpecialSoupItem(new Item.Properties().stacksTo(1)
                    .food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.0F).alwaysEdible().usingConvertsTo(Items.BOWL).build())));

    public static void register() {
        ItemGroupRegistry.MAIN_EXTRAS.add(SPECIAL_STEW);
    }
}
