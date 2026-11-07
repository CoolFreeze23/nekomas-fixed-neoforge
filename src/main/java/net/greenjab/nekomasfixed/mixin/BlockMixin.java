package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    // Magma blocks broken without Silk Touch release their lava.
    @Inject(method = "playerDestroy", at = @At("HEAD"))
    private void customAfterBreak(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        if (state.is(Blocks.MAGMA_BLOCK) && player != null && !level.isClientSide() && ModConfigValues.magmaBlockNeedsSilkTouch.get()) {
            ItemStack stack = player.getMainHandItem();
            Holder<Enchantment> silkTouch = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH);
            if (level.getRandom().nextInt(5) == 0 && !stack.isEnchanted() && stack.getEnchantments().getLevel(silkTouch) > 0
                    || !stack.getEnchantments().keySet().contains(silkTouch) || player.getMainHandItem().isEmpty()) {
                level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
            }
        }
    }
}
