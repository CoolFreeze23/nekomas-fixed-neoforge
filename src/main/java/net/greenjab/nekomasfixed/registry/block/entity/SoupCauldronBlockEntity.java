package net.greenjab.nekomasfixed.registry.block.entity;

import net.greenjab.nekomasfixed.registry.registries.SoupRegistry;
import net.greenjab.nekomasfixed.util.SoupCauldronAnimator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SoupCauldronBlockEntity extends BlockEntity implements LidBlockEntity {
    private final List<ItemStack> inputs = new ArrayList<>();
    public boolean hasStirred = false;
    private final SoupCauldronAnimator cookingAnimator = new SoupCauldronAnimator();

    public SoupCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(SoupRegistry.SOUP_CAULDRON_BLOCK_ENTITY, pos, state);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean addInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        setChanged();

        if (stack.getComponentsPatch().get(DataComponents.POTION_CONTENTS) == null) {
            for (ItemStack existing : inputs) if (ItemStack.isSameItem(existing, stack)) return false;
        }

        if (inputs.size() < 4) {
            inputs.add(stack.copyWithCount(1));
            if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        return true;
    }

    public ItemStack removeInput() {
        if (inputs.isEmpty()) return ItemStack.EMPTY;
        setChanged();

        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        ItemStack removed = inputs.removeLast();
        if (inputs.isEmpty() && level != null) {
            level.setBlockAndUpdate(worldPosition, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        }
        return removed;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("HasStirred", hasStirred);
        ListTag list = new ListTag();
        for (ItemStack input : inputs) {
            list.add(input.save(registries));
        }
        tag.put("inputs", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        hasStirred = tag.getBoolean("HasStirred");
        inputs.clear();
        ListTag list = tag.getList("inputs", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack.parse(registries, list.getCompound(i)).ifPresent(inputs::add);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    /** What the ingredient turns into when it cooks: its smelting result, except chorus fruit, which stays itself. */
    public static ItemStack cooked(ItemStack item, Level level) {
        SingleRecipeInput input = new SingleRecipeInput(item);
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level);
        if (recipe.isPresent() && !item.is(Items.CHORUS_FRUIT)) {
            ItemStack result = recipe.get().value().assemble(input, level.registryAccess());
            if (!result.isEmpty()) return result;
        }
        return item;
    }

    public void setStirred(Level level) {
        this.hasStirred = true;
        if (!level.isClientSide()) {
            List<ItemStack> updatedInputs = new ArrayList<>();
            for (ItemStack item : inputs) {
                updatedInputs.add(cooked(item, level));
            }
            inputs.clear();
            inputs.addAll(updatedInputs);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        setChanged();
    }

    public List<ItemStack> getInputs() {
        return inputs;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SoupCauldronBlockEntity blockEntity) {
        blockEntity.cookingAnimator.setStarted(blockEntity.hasStirred);
        blockEntity.cookingAnimator.step();
        float progress = blockEntity.cookingAnimator.getProgress(0);
        if (progress > 0 && progress < 1) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5 + level.getRandom().nextGaussian() * 0.25, pos.getY() + 1, pos.getZ() + 0.5 + level.getRandom().nextGaussian() * 0.25, 0.0, 0.0, 0.0);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public float getOpenNess(float tickProgress) {
        return this.cookingAnimator.getProgress(tickProgress);
    }
}
