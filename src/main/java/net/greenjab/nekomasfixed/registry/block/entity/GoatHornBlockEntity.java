package net.greenjab.nekomasfixed.registry.block.entity;

import net.greenjab.nekomasfixed.registry.block.GoatHornBlock;
import net.greenjab.nekomasfixed.registry.registries.GoatHornRegistry;
import net.greenjab.nekomasfixed.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class GoatHornBlockEntity extends BlockEntity {
    public BlockState storedTorchState = Blocks.AIR.defaultBlockState();
    public Direction facing = Direction.NORTH;

    public boolean waterlogged = false;

    public GoatHornBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(GoatHornRegistry.GOAT_HORN_BLOCK_ENTITY, worldPosition, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GoatHornBlockEntity blockEntity) {
        if (level.getBlockEntity(pos) instanceof GoatHornBlockEntity) {
            blockEntity.setWaterlogged(state.getValue(GoatHornBlock.WATERLOGGED));
            blockEntity.setFacing(state.getValue(GoatHornBlock.FACING));
        }
    }

    @Override
    protected void saveAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Torch", NbtUtils.writeBlockState(storedTorchState));
    }

    @Override
    protected void loadAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.storedTorchState = tag.contains("Torch")
                ? NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("Torch"))
                : Blocks.AIR.defaultBlockState();
    }

    public BlockState getTorch() {
        return this.storedTorchState;
    }

    public void deleteStoredTorch() {
        this.storedTorchState = Blocks.AIR.defaultBlockState();
    }

    public Direction getFacing() {
        return facing;
    }

    public void setFacing(Direction newDir) {
        facing = newDir;
    }

    public boolean isWaterLogged() {
        return waterlogged;
    }

    public void setWaterlogged(boolean logging) {
        this.waterlogged = logging;
    }

    public void setTorch(BlockState torch) {
        if (torch.is(ModTags.TORCHES) && !torch.is(storedTorchState.getBlock())) {
            storedTorchState = torch;
        }
    }

    public void setTorch(ItemStack torch) {
        if (torch.getItem() instanceof BlockItem item) {
            BlockState state = item.getBlock().defaultBlockState();
            if (state.is(ModTags.TORCHES) && !state.is(storedTorchState.getBlock())) {
                storedTorchState = state;
            }
        }
    }
}
