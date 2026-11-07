package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.block.SulfurFireBlock;
import net.greenjab.nekomasfixed.registry.registries.SulfurRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    private static void getState(BlockGetter level, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (SulfurFireBlock.canSurviveOnBlock(level.getBlockState(pos.below()))) {
            cir.setReturnValue(SulfurRegistry.SULFUR_FIRE.defaultBlockState());
        }
    }
}
