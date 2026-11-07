package net.greenjab.nekomasfixed.registry.other;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.greenjab.nekomasfixed.registry.block.entity.PotMaps;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public record PotFaceDecoration(Optional<BlockState> potFaceBlock) {
    public static final PotFaceDecoration FALLBACK_BRICKS = new PotFaceDecoration(Blocks.BRICKS.defaultBlockState());
    public static final PotFaceDecoration EMPTY = new PotFaceDecoration(Optional.empty());
    private static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE_CODEC = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);
    private static final StreamCodec<ByteBuf, Optional<BlockState>> OPTIONAL_BLOCK_STATE_CODEC = ByteBufCodecs.optional(BLOCK_STATE_CODEC);

    public static final Codec<PotFaceDecoration> CODEC = BlockState.CODEC.optionalFieldOf("block")
            .xmap(PotFaceDecoration::new, PotFaceDecoration::potFaceBlock).codec();
    public static final StreamCodec<ByteBuf, PotFaceDecoration> STREAM_CODEC = OPTIONAL_BLOCK_STATE_CODEC
            .map(PotFaceDecoration::new, PotFaceDecoration::potFaceBlock);

    public PotFaceDecoration(BlockState state) {
        this(Optional.of(state));
    }

    public Block getSafeBlock() {
        return (potFaceBlock.isEmpty() || potFaceBlock.get().isAir() ? Blocks.BRICKS.defaultBlockState() : this.potFaceBlock.get()).getBlock();
    }

    public boolean equals(Object e) {
        return e instanceof PotFaceDecoration decoration && decoration.getSafeBlock().equals(this.getSafeBlock());
    }

    public int hashCode() {
        return getSafeBlock().hashCode();
    }

    public void addToTooltip(List<Component> tooltip) {
        tooltip.add(CommonComponents.EMPTY);
        tooltip.add(Component.translatable("terracotta_decorated_pot.made_of").append(getSafeBlock().getName())
                .withColor(PotMaps.POT_FACE_TO_COLOR_MAPPING.getOrDefault(getSafeBlock(), 0xFFFFFF)));
    }
}
