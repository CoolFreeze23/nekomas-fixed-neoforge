package net.greenjab.nekomasfixed.registry.block;

import com.mojang.serialization.MapCodec;
import net.greenjab.nekomasfixed.screen.FletchingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FletchingTableBlock extends net.minecraft.world.level.block.FletchingTableBlock {
    public static final MapCodec<net.minecraft.world.level.block.FletchingTableBlock> MOD_CODEC = simpleCodec(FletchingTableBlock::new);
    private static final Component CONTAINER_TITLE = Component.translatable("container.nekomasfixed.fletching");

    public FletchingTableBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<net.minecraft.world.level.block.FletchingTableBlock> codec() {
        return MOD_CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            player.openMenu(state.getMenuProvider(level, pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected MenuProvider getMenuProvider(final BlockState state, final Level level, final BlockPos pos) {
        return new SimpleMenuProvider((containerId, inventory, player) -> new FletchingMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE);
    }
}
