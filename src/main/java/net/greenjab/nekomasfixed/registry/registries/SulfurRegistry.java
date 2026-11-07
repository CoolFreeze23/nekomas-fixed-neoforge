package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.SulfurFireBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

/** Sulfur light sources and the golden chain. Sulfur itself is a newer vanilla material: these are crafted with gunpowder here. */
public class SulfurRegistry {
    // The noxious gas particle is newer vanilla too. This one is drawn from the smoke sprites in yellow-green.
    public static final SimpleParticleType NOXIOUS_GAS = Registry.register(BuiltInRegistries.PARTICLE_TYPE, NekomasFixed.id("noxious_gas"), new SimpleParticleType(false));

    public static final Block SULFUR_FIRE = block("sulfur_fire", new SulfurFireBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE).replaceable().noCollission().instabreak().lightLevel(state -> 10).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));
    public static final Block SULFUR_LANTERN = block("sulfur_lantern", new LanternBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).forceSolidOn().strength(3.5F).sound(SoundType.LANTERN).lightLevel(state -> 10).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final Block SULFUR_CAMPFIRE = block("sulfur_campfire", new CampfireBlock(false, 2, BlockBehaviour.Properties.of()
            .mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD)
            .lightLevel(state -> state.getValue(CampfireBlock.LIT) ? 10 : 0).noOcclusion().ignitedByLava()));
    public static final Block SULFUR_TORCH = block("sulfur_torch", new TorchBlock(NOXIOUS_GAS, BlockBehaviour.Properties.of()
            .noCollission().instabreak().lightLevel(state -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)));
    public static final Block WALL_SULFUR_TORCH = block("wall_sulfur_torch", new WallTorchBlock(NOXIOUS_GAS, BlockBehaviour.Properties.of()
            .noCollission().instabreak().lightLevel(state -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY).dropsLike(SULFUR_TORCH)));
    public static final Block GOLD_CHAIN = block("gold_chain", new ChainBlock(BlockBehaviour.Properties.of()
            .forceSolidOn().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.CHAIN).noOcclusion()));

    public static final Item SULFUR_LANTERN_ITEM = item("sulfur_lantern", new BlockItem(SULFUR_LANTERN, new Item.Properties()));
    public static final Item GOLD_CHAIN_ITEM = item("gold_chain", new BlockItem(GOLD_CHAIN, new Item.Properties()));
    public static final Item SULFUR_CAMPFIRE_ITEM = item("sulfur_campfire", new BlockItem(SULFUR_CAMPFIRE, new Item.Properties()));
    public static final Item SULFUR_TORCH_ITEM = item("sulfur_torch", new StandingAndWallBlockItem(SULFUR_TORCH, WALL_SULFUR_TORCH, new Item.Properties(), Direction.DOWN));

    private static Block block(String id, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, NekomasFixed.id(id), block);
    }

    private static Item item(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id(id), item);
    }

    public static void attachBlockEntities(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.CAMPFIRE, SULFUR_CAMPFIRE);
    }

    public static void register() {
        ItemGroupRegistry.MAIN_EXTRAS.add(SULFUR_TORCH_ITEM);
        ItemGroupRegistry.MAIN_EXTRAS.add(SULFUR_LANTERN_ITEM);
        ItemGroupRegistry.MAIN_EXTRAS.add(GOLD_CHAIN_ITEM);
        ItemGroupRegistry.MAIN_EXTRAS.add(SULFUR_CAMPFIRE_ITEM);
    }
}
