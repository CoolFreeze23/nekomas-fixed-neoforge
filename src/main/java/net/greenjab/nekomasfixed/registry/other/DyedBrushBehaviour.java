package net.greenjab.nekomasfixed.registry.other;

import net.greenjab.nekomasfixed.registry.item.DyedBrushItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DyedBrushBehaviour extends OptionalDispenseItemBehavior {

    @Override
    protected ItemStack execute(BlockSource source, ItemStack dispensed) {
        this.setSuccess(false);
        if (dispensed.getItem() instanceof DyedBrushItem brushItem) {
            BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            Level level = source.level();
            if (DyedBrushItem.applyDye(level, pos, level.getBlockState(pos), brushItem.getColor())) {
                this.setSuccess(true);
                level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (dispensed.getDamageValue() + 1 >= dispensed.getMaxDamage()) return ItemStack.EMPTY;
                dispensed.setDamageValue(dispensed.getDamageValue() + 1);
            }
        }
        return dispensed;
    }
}
