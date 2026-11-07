package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.util.AllDyes;
import net.greenjab.nekomasfixed.util.BlockDyeMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.EnumMap;
import java.util.Map;

/**
 * Wool, spotted wool and concrete slabs and stairs in all 20 colours. Newer vanilla versions have the 16 standard colours
 * of wool and concrete slabs and stairs themselves, this version doesn't, so they are registered here too.
 */
public class ColourBlockRegistry {
    // The in-game order of the creative tab, the same order main uses for its colour lists.
    private static final AllDyes[] TAB_ORDER = {
            AllDyes.WHITE, AllDyes.LIGHT_GRAY, AllDyes.GRAY, AllDyes.BLACK, AllDyes.BROWN, AllDyes.RED, AllDyes.ORANGE, AllDyes.YELLOW,
            AllDyes.LIME, AllDyes.GREEN, AllDyes.CYAN, AllDyes.LIGHT_BLUE, AllDyes.BLUE, AllDyes.PURPLE, AllDyes.MAGENTA, AllDyes.PINK,
            AllDyes.AMBER, AllDyes.AQUA, AllDyes.INDIGO, AllDyes.MAROON};

    public static void register() {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        for (AllDyes dye : TAB_ORDER) {
            Block wool = BlockDyeMap.WOOL.get(dye);
            Block slab = slab(dye.getSerializedName() + "_wool_slab", woolSlabProperties(dye));
            Block stairs = stairs(dye.getSerializedName() + "_wool_stairs", wool);
            BlockDyeMap.WOOL_SLABS.put(dye, slab);
            BlockDyeMap.WOOL_STAIRS.put(dye, stairs);
            fire.setFlammable(slab, 30, 60);
            fire.setFlammable(stairs, 30, 60);
        }
        for (AllDyes dye : TAB_ORDER) {
            Block wool = BlockDyeMap.SPOTTED_WOOL.get(dye);
            Block slab = slab(dye.getSerializedName() + "_spotted_wool_slab", BlockBehaviour.Properties.ofFullCopy(wool));
            Block stairs = stairs(dye.getSerializedName() + "_spotted_wool_stairs", wool);
            BlockDyeMap.SPOTTED_WOOL_SLABS.put(dye, slab);
            BlockDyeMap.SPOTTED_WOOL_STAIRS.put(dye, stairs);
            fire.setFlammable(slab, 30, 60);
            fire.setFlammable(stairs, 30, 60);
        }
        for (AllDyes dye : TAB_ORDER) {
            Block concrete = BlockDyeMap.CONCRETE.get(dye);
            BlockDyeMap.CONCRETE_SLABS.put(dye, slab(dye.getSerializedName() + "_concrete_slab", concreteSlabProperties(dye)));
            BlockDyeMap.CONCRETE_STAIRS.put(dye, stairs(dye.getSerializedName() + "_concrete_stairs", concrete));
        }
    }

    private static Block slab(String id, BlockBehaviour.Properties properties) {
        Block block = Registry.register(BuiltInRegistries.BLOCK, NekomasFixed.id(id), new SlabBlock(properties));
        item(id, block);
        return block;
    }

    private static Block stairs(String id, Block base) {
        Block block = Registry.register(BuiltInRegistries.BLOCK, NekomasFixed.id(id),
                new StairBlock(base.defaultBlockState(), BlockBehaviour.Properties.ofLegacyCopy(base)));
        item(id, block);
        return block;
    }

    private static void item(String id, Block block) {
        Item item = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id(id), new BlockItem(block, new Item.Properties()));
        ItemGroupRegistry.COLOUR_EXTRAS.add(item);
    }

    private static BlockBehaviour.Properties woolSlabProperties(AllDyes dye) {
        return BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL)
                .ignitedByLava().mapColor(mapColour(dye));
    }

    private static BlockBehaviour.Properties concreteSlabProperties(AllDyes dye) {
        return BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
                .mapColor(mapColour(dye));
    }

    private static final Map<AllDyes, DyeColor> EXTRA_MAP_COLOURS = new EnumMap<>(Map.of(
            AllDyes.AMBER, DyeColor.YELLOW, AllDyes.AQUA, DyeColor.LIGHT_BLUE, AllDyes.INDIGO, DyeColor.MAGENTA, AllDyes.MAROON, DyeColor.RED));

    private static DyeColor mapColour(AllDyes dye) {
        DyeColor extra = EXTRA_MAP_COLOURS.get(dye);
        return extra != null ? extra : DyeColor.byName(dye.getSerializedName(), DyeColor.WHITE);
    }
}
