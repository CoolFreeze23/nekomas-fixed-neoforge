package net.greenjab.nekomasfixed.registry.item;

import net.greenjab.nekomasfixed.util.AllDyes;
import net.greenjab.nekomasfixed.util.BlockDyeMap;
import net.greenjab.nekomasfixed.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.nbt.CompoundTag;

public class DyedBrushItem extends Item {
    private final AllDyes color;

    public DyedBrushItem(AllDyes color, Properties settings) {
        super(settings);
        this.color = color;
    }

    public AllDyes getColor() {
        return color;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player != null) player.swing(context.getHand());
        if (!level.isClientSide() && applyDye(level, pos, level.getBlockState(pos), color)) {
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    /** Recolours the block if it can be dyed and isn't this colour yet. Brushes in hand and in dispensers both come through here. */
    public static boolean applyDye(Level level, BlockPos pos, BlockState state, AllDyes color) {
        if (!state.is(ModTags.CAN_BE_DYED_WITH_BRUSH)) return false;

        if (state.is(Blocks.TERRACOTTA) || state.is(BlockTags.TERRACOTTA) && !state.is(BlockDyeMap.TERRACOTTA.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.TERRACOTTA.get(color).defaultBlockState());
        } else if (state.is(ModTags.DYED_BRICKS) || state.is(Blocks.BRICKS) && !state.is(BlockDyeMap.BRICKS.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.BRICKS.get(color).defaultBlockState());
        } else if (state.is(ModTags.DYED_BRICK_SLABS) || state.is(Blocks.BRICK_SLAB) && !state.is(BlockDyeMap.BRICK_SLAB.get(color))) {
            level.setBlockAndUpdate(pos, slab(state, BlockDyeMap.BRICK_SLAB.get(color)));
        } else if (state.is(ModTags.DYED_BRICK_STAIRS) || state.is(Blocks.BRICK_STAIRS) && !state.is(BlockDyeMap.BRICK_STAIRS.get(color))) {
            level.setBlockAndUpdate(pos, stairs(state, BlockDyeMap.BRICK_STAIRS.get(color)));
        } else if (state.is(ModTags.DYED_BRICK_WALLS) || state.is(Blocks.BRICK_WALL) && !state.is(BlockDyeMap.BRICK_WALL.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.BRICK_WALL.get(color).defaultBlockState()
                    .setValue(WallBlock.WATERLOGGED, state.getValue(WallBlock.WATERLOGGED))
                    .setValue(WallBlock.NORTH_WALL, state.getValue(WallBlock.NORTH_WALL))
                    .setValue(WallBlock.EAST_WALL, state.getValue(WallBlock.EAST_WALL))
                    .setValue(WallBlock.SOUTH_WALL, state.getValue(WallBlock.SOUTH_WALL))
                    .setValue(WallBlock.WEST_WALL, state.getValue(WallBlock.WEST_WALL))
                    .setValue(WallBlock.UP, state.getValue(WallBlock.UP)));
        } else if (state.is(ModTags.STAINED_GLASSES) || state.is(Blocks.GLASS) && !state.is(BlockDyeMap.STAINED_GLASS.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.STAINED_GLASS.get(color).defaultBlockState());
        } else if (state.is(ModTags.STAINED_GLASS_PANES) || state.is(Blocks.GLASS_PANE) && !state.is(BlockDyeMap.STAINED_GLASS_PANE.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.STAINED_GLASS_PANE.get(color).defaultBlockState()
                    .setValue(StainedGlassPaneBlock.WATERLOGGED, state.getValue(StainedGlassPaneBlock.WATERLOGGED))
                    .setValue(StainedGlassPaneBlock.EAST, state.getValue(StainedGlassPaneBlock.EAST))
                    .setValue(StainedGlassPaneBlock.WEST, state.getValue(StainedGlassPaneBlock.WEST))
                    .setValue(StainedGlassPaneBlock.SOUTH, state.getValue(StainedGlassPaneBlock.SOUTH))
                    .setValue(StainedGlassPaneBlock.NORTH, state.getValue(StainedGlassPaneBlock.NORTH)));
        } else if (state.is(ModTags.GLAZED_TERRACOTTAS) && !state.is(BlockDyeMap.GLAZED_TERRACOTTA.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.GLAZED_TERRACOTTA.get(color).defaultBlockState()
                    .setValue(GlazedTerracottaBlock.FACING, state.getValue(GlazedTerracottaBlock.FACING)));
        } else if (state.is(ModTags.SPOTTED_WOOLS) && !state.is(BlockDyeMap.SPOTTED_WOOL.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.SPOTTED_WOOL.get(color).defaultBlockState());
        } else if (state.is(BlockTags.WOOL) && !state.is(ModTags.SPOTTED_WOOLS) && !state.is(BlockDyeMap.WOOL.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.WOOL.get(color).defaultBlockState());
        } else if (state.is(BlockTags.CANDLES) || state.is(Blocks.CANDLE) && !state.is(BlockDyeMap.CANDLE.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.CANDLE.get(color).defaultBlockState()
                    .setValue(CandleBlock.CANDLES, state.getValue(CandleBlock.CANDLES))
                    .setValue(CandleBlock.LIT, state.getValue(CandleBlock.LIT)));
        } else if (state.is(ModTags.SPOTTED_CARPETS) && !state.is(BlockDyeMap.SPOTTED_CARPET.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.SPOTTED_CARPET.get(color).defaultBlockState());
        } else if (state.is(BlockTags.WOOL_CARPETS) && !state.is(ModTags.SPOTTED_CARPETS) && !state.is(BlockDyeMap.CARPET.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.CARPET.get(color).defaultBlockState());
        } else if (state.is(ModTags.CONCRETES) && !state.is(BlockDyeMap.CONCRETE.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.CONCRETE.get(color).defaultBlockState());
        } else if (state.is(ModTags.CONCRETE_POWDERS) && !state.is(BlockDyeMap.CONCRETE_POWDER.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.CONCRETE_POWDER.get(color).defaultBlockState());
        } else if (state.is(ModTags.FROGLIGHTS) && !state.is(BlockDyeMap.FROGLIGHT.get(color))) {
            level.setBlockAndUpdate(pos, BlockDyeMap.FROGLIGHT.get(color).defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
        } else if (state.is(Blocks.SHULKER_BOX) || state.is(BlockTags.SHULKER_BOXES) && !state.is(BlockDyeMap.SHULKER_BOX.get(color))) {
            if (level.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity oldBox) {
                CompoundTag contents = oldBox.saveCustomOnly(level.registryAccess());
                level.setBlockAndUpdate(pos, BlockDyeMap.SHULKER_BOX.get(color).defaultBlockState().setValue(ShulkerBoxBlock.FACING, state.getValue(ShulkerBoxBlock.FACING)));
                if (level.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity newBox) {
                    newBox.loadCustomOnly(contents, level.registryAccess());
                    newBox.setChanged();
                }
            }
        } else if (state.is(BlockTags.BEDS) && !state.is(BlockDyeMap.BED.get(color))) {
            Block bed = BlockDyeMap.BED.get(color);
            BedPart bedPart = state.getValue(BedBlock.PART);
            Direction bedDir = state.getValue(BedBlock.FACING);
            BlockPos otherPos = pos.relative(bedPart == BedPart.FOOT ? bedDir : bedDir.getOpposite());
            BlockState newBed = bed.defaultBlockState().setValue(BedBlock.FACING, bedDir);
            if (level.getBlockState(otherPos).is(BlockTags.BEDS)) {
                BlockPos head = bedPart == BedPart.HEAD ? pos : otherPos;
                BlockPos foot = bedPart == BedPart.HEAD ? otherPos : pos;
                level.setBlock(head, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(foot, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlockAndUpdate(head, newBed.setValue(BedBlock.PART, BedPart.HEAD));
                level.setBlockAndUpdate(foot, newBed.setValue(BedBlock.PART, BedPart.FOOT));
            } else {
                level.setBlockAndUpdate(pos, newBed.setValue(BedBlock.PART, bedPart));
            }
        } else if (state.is(ModTags.WOOL_STAIRS) && !state.is(BlockDyeMap.WOOL_STAIRS.get(color))) {
            level.setBlockAndUpdate(pos, stairs(state, BlockDyeMap.WOOL_STAIRS.get(color)));
        } else if (state.is(ModTags.WOOL_SLABS) && !state.is(BlockDyeMap.WOOL_SLABS.get(color))) {
            level.setBlockAndUpdate(pos, slab(state, BlockDyeMap.WOOL_SLABS.get(color)));
        } else if (state.is(ModTags.CONCRETE_SLABS) && !state.is(BlockDyeMap.CONCRETE_SLABS.get(color))) {
            level.setBlockAndUpdate(pos, slab(state, BlockDyeMap.CONCRETE_SLABS.get(color)));
        } else if (state.is(ModTags.CONCRETE_STAIRS) && !state.is(BlockDyeMap.CONCRETE_STAIRS.get(color))) {
            level.setBlockAndUpdate(pos, stairs(state, BlockDyeMap.CONCRETE_STAIRS.get(color)));
        } else if (state.is(ModTags.SPOTTED_WOOL_SLABS) && !state.is(BlockDyeMap.SPOTTED_WOOL_SLABS.get(color))) {
            level.setBlockAndUpdate(pos, slab(state, BlockDyeMap.SPOTTED_WOOL_SLABS.get(color)));
        } else if (state.is(ModTags.SPOTTED_WOOL_STAIRS) && !state.is(BlockDyeMap.SPOTTED_WOOL_STAIRS.get(color))) {
            level.setBlockAndUpdate(pos, stairs(state, BlockDyeMap.SPOTTED_WOOL_STAIRS.get(color)));
        } else {
            return false;
        }
        return true;
    }

    private static BlockState slab(BlockState from, Block to) {
        return to.defaultBlockState()
                .setValue(SlabBlock.WATERLOGGED, from.getValue(SlabBlock.WATERLOGGED))
                .setValue(SlabBlock.TYPE, from.getValue(SlabBlock.TYPE));
    }

    private static BlockState stairs(BlockState from, Block to) {
        return to.defaultBlockState()
                .setValue(StairBlock.WATERLOGGED, from.getValue(StairBlock.WATERLOGGED))
                .setValue(StairBlock.FACING, from.getValue(StairBlock.FACING))
                .setValue(StairBlock.HALF, from.getValue(StairBlock.HALF))
                .setValue(StairBlock.SHAPE, from.getValue(StairBlock.SHAPE));
    }
}
