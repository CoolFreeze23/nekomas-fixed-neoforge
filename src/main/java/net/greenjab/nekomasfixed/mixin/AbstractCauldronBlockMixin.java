package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.block.cauldron.SoupCauldronBlock;
import net.greenjab.nekomasfixed.registry.block.entity.SoupCauldronBlockEntity;
import net.greenjab.nekomasfixed.registry.registries.SoupRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCauldronBlock.class)
public class AbstractCauldronBlockMixin {

    /** A food item in a full water cauldron over a fire starts a soup. */
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onCauldronUse(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (state.is(Blocks.WATER_CAULDRON) && ((LayeredCauldronBlock) state.getBlock()).isFull(state)
                && SoupCauldronBlock.isHeated(level, pos) && SoupCauldronBlock.FOOD_COLORS.containsKey(itemStack.getItem())) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, SoupRegistry.SOUP_CAULDRON.defaultBlockState());
                if (level.getBlockEntity(pos) instanceof SoupCauldronBlockEntity soup && soup.addInput(itemStack.copyWithCount(1))) {
                    itemStack.consume(1, player);
                }
            }
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
        }
    }
}
