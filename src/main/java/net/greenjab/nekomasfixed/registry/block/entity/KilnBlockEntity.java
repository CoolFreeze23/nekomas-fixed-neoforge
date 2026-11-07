package net.greenjab.nekomasfixed.registry.block.entity;

import net.greenjab.nekomasfixed.registry.registries.KilnRegistry;
import net.greenjab.nekomasfixed.screen.KilnMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class KilnBlockEntity extends AbstractFurnaceBlockEntity {
    private static final Component CONTAINER_NAME_TEXT = Component.translatable("container.nekomasfixed.kiln");

    public KilnBlockEntity(BlockPos pos, BlockState state) {
        super(KilnRegistry.KILN_BLOCK_ENTITY, pos, state, KilnRegistry.KILN_RECIPE);
    }

    @Override
    protected Component getDefaultName() {
        return CONTAINER_NAME_TEXT;
    }

    @Override
    protected int getBurnDuration(ItemStack fuelItem) {
        return super.getBurnDuration(fuelItem) / 2;
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new KilnMenu(syncId, playerInventory, this, this.dataAccess);
    }
}
