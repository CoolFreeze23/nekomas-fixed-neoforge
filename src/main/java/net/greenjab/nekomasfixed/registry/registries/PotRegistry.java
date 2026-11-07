package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.TerracottaDecoratedPotBlock;
import net.greenjab.nekomasfixed.registry.block.entity.PotMaps;
import net.greenjab.nekomasfixed.registry.block.entity.TerracottaDecoratedPotBlockEntity;
import net.greenjab.nekomasfixed.registry.other.PotEngravingDecoration;
import net.greenjab.nekomasfixed.registry.other.PotFaceDecoration;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class PotRegistry {
    // Under minecraft:, as main has them, so the loot table of the pot reads the same.
    public static final DataComponentType<PotEngravingDecoration> POT_ENGRAVED_DECORATIONS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.withDefaultNamespace("pot_engraved_decorations"),
            DataComponentType.<PotEngravingDecoration>builder()
                    .persistent(PotEngravingDecoration.CODEC)
                    .networkSynchronized(PotEngravingDecoration.STREAM_CODEC)
                    .cacheEncoding()
                    .build());
    public static final DataComponentType<PotFaceDecoration> POT_FACE_BLOCK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.withDefaultNamespace("pot_face_block"),
            DataComponentType.<PotFaceDecoration>builder()
                    .persistent(PotFaceDecoration.CODEC)
                    .networkSynchronized(PotFaceDecoration.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    public static final Block TERRACOTTA_DECORATED_POT = Registry.register(BuiltInRegistries.BLOCK, NekomasFixed.id("terracotta_decorated_pot"),
            new TerracottaDecoratedPotBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).strength(0.0F, 0.0F)
                    .pushReaction(PushReaction.DESTROY).noOcclusion()));
    public static final Item TERRACOTTA_DECORATED_POT_ITEM = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("terracotta_decorated_pot"),
            new BlockItem(TERRACOTTA_DECORATED_POT, new Item.Properties()
                    .component(POT_ENGRAVED_DECORATIONS, PotEngravingDecoration.EMPTY)
                    .component(POT_FACE_BLOCK, PotFaceDecoration.FALLBACK_BRICKS)
                    .component(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY)
                    .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)));
    public static final BlockEntityType<TerracottaDecoratedPotBlockEntity> TERRACOTTA_DECORATED_POT_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, NekomasFixed.id("terracotta_decorated_pot"),
            BlockEntityType.Builder.of(TerracottaDecoratedPotBlockEntity::new, TERRACOTTA_DECORATED_POT).build(null));

    public static void register() {
        PotMaps.register();
        ItemGroupRegistry.MAIN_EXTRAS.add(TERRACOTTA_DECORATED_POT_ITEM);
    }

    /** Terracotta pots sit beside vanilla's decorated pot in the vanilla tabs too. */
    public static void onBuildCreativeTabs(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS)
                || event.getTabKey().equals(net.minecraft.world.item.CreativeModeTabs.REDSTONE_BLOCKS)) {
            event.accept(TERRACOTTA_DECORATED_POT_ITEM);
        }
    }
}
