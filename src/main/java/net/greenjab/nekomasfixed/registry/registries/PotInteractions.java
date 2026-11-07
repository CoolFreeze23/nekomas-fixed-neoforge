package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.registry.block.TerracottaDecoratedPotBlock;
import net.greenjab.nekomasfixed.registry.block.entity.TerracottaDecoratedPotBlockEntity;
import net.greenjab.nekomasfixed.registry.other.PotFaceDecoration;
import net.greenjab.nekomasfixed.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;

/** Crouching on a pot: shears take an engraving off, a pot face block changes the face, an engraving item engraves that side. */
public class PotInteractions {

    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = interact(event.getEntity(), event.getLevel(), event.getItemStack(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    public static InteractionResult interact(Player player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        if (stack.isEmpty() || !player.isCrouching() || !(level.getBlockEntity(pos) instanceof TerracottaDecoratedPotBlockEntity decoratedPot)) {
            return InteractionResult.PASS;
        }
        Direction hitDir = hit.getDirection();
        BlockState state = level.getBlockState(pos);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        if (stack.is(Items.SHEARS) && !TerracottaDecoratedPotBlock.isEmptySide(hitDir, decoratedPot, state)) {
            String hitSide = TerracottaDecoratedPotBlock.getSideNameFromHit(hitDir, facing);
            var engraved = decoratedPot.getEngravingDecorations();
            Optional<Item> itemToDrop = switch (hitSide) {
                case "front" -> engraved.front();
                case "back" -> engraved.back();
                case "left" -> engraved.left();
                case "right" -> engraved.right();
                default -> Optional.empty();
            };
            if (itemToDrop.isPresent()) {
                if (!level.isClientSide) {
                    decoratedPot.engravingDecorations = engraved.removeEngraving(hitSide);
                    drop(level, pos, itemToDrop.get().getDefaultInstance());
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    changed(level, pos, state, decoratedPot);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().is(ModTags.CAN_BECOME_POT_FACE)) {
            PotFaceDecoration newFace = new PotFaceDecoration(blockItem.getBlock().defaultBlockState());
            if (!decoratedPot.getPotFace().equals(newFace)) {
                if (!level.isClientSide) {
                    drop(level, pos, decoratedPot.getPotFace().getSafeBlock().asItem().getDefaultInstance());
                    decoratedPot.faceDecoration = newFace;
                    stack.consume(1, player);
                    changed(level, pos, state, decoratedPot);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        if (stack.is(ModTags.CAN_BE_ENGRAVED) && !TerracottaDecoratedPotBlock.isEmptySide(hitDir, decoratedPot, state)) {
            Item engravedItem = decoratedPot.getEngravingDecorations().getItemEngravedAt(hitDir, facing);
            if (engravedItem == stack.getItem()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                decoratedPot.engravingDecorations = decoratedPot.getEngravingDecorations().engraveSideFacing(hitDir, facing, stack.getItem());
                stack.consume(1, player);
                changed(level, pos, state, decoratedPot);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static void drop(Level level, BlockPos pos, ItemStack stack) {
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
    }

    private static void changed(Level level, BlockPos pos, BlockState state, TerracottaDecoratedPotBlockEntity pot) {
        pot.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }
}
