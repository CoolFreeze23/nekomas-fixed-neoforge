package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.greenjab.nekomasfixed.util.MessyBedAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin extends Block implements MessyBedAccessor {

    private BedBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setDefaultState(DyeColor color, BlockBehaviour.Properties properties, CallbackInfo ci) {
        if (this.defaultBlockState().hasProperty(MessyBedAccessor.MESSY)) {
            this.registerDefaultState(this.defaultBlockState().setValue(MessyBedAccessor.MESSY, false));
        }
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void appendProperties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(MessyBedAccessor.MESSY);
    }

    /** Using a made bed at night messes it up. Crouching with an empty hand makes it again. */
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide() && ModConfigValues.enableMessyBeds.get()) {
            BlockPos otherPos = state.getValue(BedBlock.PART) == BedPart.FOOT ? pos.relative(state.getValue(BedBlock.FACING)) : pos.relative(state.getValue(BedBlock.FACING).getOpposite());

            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && state.getValue(MessyBedAccessor.MESSY)) {
                setMessy(level, pos, state, otherPos, false);
                player.swing(InteractionHand.MAIN_HAND, true);
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            if (!level.isDay() && !state.getValue(MessyBedAccessor.MESSY) && !state.getValue(BedBlock.OCCUPIED)) {
                setMessy(level, pos, state, otherPos, true);
            }
        }
    }

    private static void setMessy(Level level, BlockPos pos, BlockState state, BlockPos otherPos, boolean messy) {
        level.setBlockAndUpdate(pos, state.setValue(MessyBedAccessor.MESSY, messy));
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.hasProperty(MessyBedAccessor.MESSY)) {
            level.setBlockAndUpdate(otherPos, otherState.setValue(MessyBedAccessor.MESSY, messy));
        }
    }
}
