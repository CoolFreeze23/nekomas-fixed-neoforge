package net.greenjab.nekomasfixed.registry.registries;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

public class BlockEntityRegistry {

    public static void attachBlockEntities(BlockEntityTypeAddBlocksEvent event) {
        attachSignBlockEntities(event);
        attachBedBlockEntities(event);
        attachShulkerBoxBlockEntities(event);
    }

    private static void attachSignBlockEntities(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN,
                BlockRegistry.BAOBAB_SIGN,
                        BlockRegistry.BAOBAB_WALL_SIGN);
        event.modify(BlockEntityType.HANGING_SIGN,
                BlockRegistry.BAOBAB_HANGING_SIGN,
                        BlockRegistry.BAOBAB_WALL_HANGING_SIGN);
    }

    // Ancient beds use vanilla BedBlock (getRenderShape ENTITYBLOCK_ANIMATED) so the vanilla
    // BedRenderer handles them; attach each to BlockEntityType.BED for the block-entity lookup.
    private static void attachBedBlockEntities(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.BED,
                BlockRegistry.AMBER_BED,
                        BlockRegistry.AQUA_BED,
                        BlockRegistry.INDIGO_BED,
                        BlockRegistry.MAROON_BED);
    }

    // Ancient shulker boxes use vanilla ShulkerBoxBlock (ENTITYBLOCK_ANIMATED) so the vanilla
    // ShulkerBoxRenderer handles them; attach each to BlockEntityType.SHULKER_BOX for storage.
    private static void attachShulkerBoxBlockEntities(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SHULKER_BOX,
                BlockRegistry.AMBER_SHULKER_BOX,
                        BlockRegistry.AQUA_SHULKER_BOX,
                        BlockRegistry.INDIGO_SHULKER_BOX,
                        BlockRegistry.MAROON_SHULKER_BOX);
    }
}
