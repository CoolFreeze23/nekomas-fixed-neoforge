package net.greenjab.nekomasfixed.registry.block;

import net.greenjab.nekomasfixed.registry.block.entity.PotMaps;
import net.greenjab.nekomasfixed.registry.block.entity.TerracottaDecoratedPotBlockEntity;
import net.greenjab.nekomasfixed.registry.block.enums.SpriteFacing;
import net.greenjab.nekomasfixed.registry.other.PotEngravingDecoration;
import net.greenjab.nekomasfixed.registry.other.PotFaceDecoration;
import net.greenjab.nekomasfixed.registry.registries.ComponentRegistry;
import net.greenjab.nekomasfixed.registry.registries.PotRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/** The pot crafting makes: engravable sherds and a face of bricks or terracotta. The rest comes from the vanilla pot. */
public class TerracottaDecoratedPotBlock extends DecoratedPotBlock {

    public TerracottaDecoratedPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof TerracottaDecoratedPotBlockEntity potEntity) {
            Block faceBlock = potEntity.getPotFace().getSafeBlock();
            if (faceBlock.defaultBlockState().is(Blocks.BRICKS)) {
                faceBlock = Blocks.TERRACOTTA;
            }
            level.levelEvent(player, 2001, pos, Block.getId(faceBlock.defaultBlockState()));
        } else {
            super.spawnDestroyParticles(level, player, pos, state);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TerracottaDecoratedPotBlockEntity decoratedPot) {
            if (level.isClientSide) {
                return ItemInteractionResult.CONSUME;
            }

            ItemStack potItem = decoratedPot.getTheItem();
            if (!itemStack.isEmpty() && (potItem.isEmpty() || ItemStack.isSameItemSameComponents(potItem, itemStack) && potItem.getCount() < potItem.getMaxStackSize())) {
                decoratedPot.wobble(TerracottaDecoratedPotBlockEntity.WobbleStyle.POSITIVE);
                player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                ItemStack awardedItem = itemStack.consumeAndReturn(1, player);
                float pitchBend;
                if (decoratedPot.isEmpty()) {
                    decoratedPot.setTheItem(awardedItem);
                    pitchBend = (float) awardedItem.getCount() / awardedItem.getMaxStackSize();
                } else {
                    potItem.grow(1);
                    pitchBend = (float) potItem.getCount() / potItem.getMaxStackSize();
                }

                level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * pitchBend);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0);
                }

                decoratedPot.setChanged();
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TerracottaDecoratedPotBlockEntity decoratedPot) {
            level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
            decoratedPot.wobble(TerracottaDecoratedPotBlockEntity.WobbleStyle.NEGATIVE);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new TerracottaDecoratedPotBlockEntity(worldPosition, blockState);
    }

    public static boolean isEmptySide(Direction ofHit, TerracottaDecoratedPotBlockEntity blockEntity, BlockState state) {
        Direction potFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        String sideName = getSideNameFromHit(ofHit, potFacing);
        if (sideName == null) return true;
        return isSideEmpty(blockEntity.getDecorations(), sideName);
    }

    public static String getSideNameFromHit(Direction hitFace, Direction potFacing) {
        if (hitFace.getAxis().isVertical()) {
            return null;
        }
        if (hitFace == potFacing) return "front";
        if (hitFace == potFacing.getOpposite()) return "back";
        if (hitFace == potFacing.getCounterClockWise()) return "left";
        if (hitFace == potFacing.getClockWise()) return "right";
        return null;
    }

    public static boolean isSideEmpty(PotDecorations decorations, String side) {
        if (decorations == null || side == null) return true;
        return switch (side.toLowerCase()) {
            case "back" -> decorations.back().isEmpty();
            case "left" -> decorations.left().isEmpty();
            case "right" -> decorations.right().isEmpty();
            case "front" -> decorations.front().isEmpty();
            default -> true;
        };
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity maybeEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (maybeEntity instanceof TerracottaDecoratedPotBlockEntity entity) {
            params.withDynamicDrop(SHERDS_DYNAMIC_DROP_ID, output -> {
                for (Item item : entity.getDecorations().ordered()) {
                    output.accept(item.getDefaultInstance());
                }
            });
        }
        return super.getDrops(state, params);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof TerracottaDecoratedPotBlockEntity pot) {
            return TerracottaDecoratedPotBlockEntity.createDecoratedPotInstance(pot.getDecorations(), pot.getEngravingDecorations(), pot.getPotFace());
        }
        return new ItemStack(PotRegistry.TERRACOTTA_DECORATED_POT_ITEM);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        PotDecorations decorations = stack.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY);
        PotEngravingDecoration engravings = stack.getOrDefault(PotRegistry.POT_ENGRAVED_DECORATIONS, PotEngravingDecoration.EMPTY);
        List<Boolean> glow = stack.getOrDefault(ComponentRegistry.SHERD_GLOW_OVERRIDES, ComponentRegistry.DEFAULT_SHERD_GLOW_OVERRIDES);
        if (!decorations.equals(PotDecorations.EMPTY)) {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(PotMaps.sideTooltip(decorations.front(), engravings.getFront(), SpriteFacing.BACK, glow.get(SpriteFacing.FRONT.ordinal)));
            tooltip.add(PotMaps.sideTooltip(decorations.left(), engravings.getLeft(), SpriteFacing.LEFT, glow.get(SpriteFacing.LEFT.ordinal)));
            tooltip.add(PotMaps.sideTooltip(decorations.right(), engravings.getRight(), SpriteFacing.RIGHT, glow.get(SpriteFacing.RIGHT.ordinal)));
            tooltip.add(PotMaps.sideTooltip(decorations.back(), engravings.getBack(), SpriteFacing.FRONT, glow.get(SpriteFacing.BACK.ordinal)));
        }
        stack.getOrDefault(PotRegistry.POT_FACE_BLOCK, PotFaceDecoration.FALLBACK_BRICKS).addToTooltip(tooltip);
    }
}
