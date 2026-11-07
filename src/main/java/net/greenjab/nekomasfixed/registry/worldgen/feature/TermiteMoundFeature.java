package net.greenjab.nekomasfixed.registry.worldgen.feature;

import com.mojang.serialization.Codec;
import net.greenjab.nekomasfixed.registry.block.entity.TermitehiveBlockEntity;
import net.greenjab.nekomasfixed.registry.registries.TermiteRegistry;
import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class TermiteMoundFeature extends Feature<NoneFeatureConfiguration> {

    public TermiteMoundFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos start = context.origin();
        if (!ModConfigValues.generateMound.get()) return false;
        int height = random.nextInt(2) + 6;

        int x, y, z;
        if (!world.getBlockState(start.below()).isRedstoneConductor(world, start.below())) return false;
        if (!world.getBlockState(start).isAir()) return false;

        float maxRadius = 3.5f - random.nextFloat() * 1.5f;

        for (y = 0; y < height - 2; y++) {
            float r = maxRadius * (1 - (y / (float) height)) - (y / (float) height);
            for (x = -(int) maxRadius; x <= maxRadius; x++) {
                for (z = -(int) maxRadius; z <= maxRadius; z++) {
                    float distSq = x * x + z * z;
                    if (distSq <= r * r) {
                        BlockPos pos = start.offset(x, y, z);

                        boolean isSurface = distSq >= (r - 1) * (r - 1);
                        boolean isSupported = world.getBlockState(pos.below()).isRedstoneConductor(world, pos.below()) && !world.getBlockState(pos.below()).is(BlockTags.REPLACEABLE);

                        if (isSurface && random.nextInt(4) == 0 && isSupported) {
                            world.setBlock(pos, TermiteRegistry.TERMITE_HIVE.defaultBlockState(), 3);
                            world.getBlockEntity(pos, TermiteRegistry.TERMITE_HIVE_BLOCK_ENTITY).ifPresent(blockEntity -> {
                                int count = 1 + random.nextInt(2);
                                for (int i = 0; i < count; i++) {
                                    blockEntity.addTermite(TermitehiveBlockEntity.TermiteData.create(random.nextInt(100)));
                                }
                            });
                        } else if (isSupported) {
                            world.setBlock(pos, TermiteRegistry.TERMITE_BLOCK.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        return true;
    }
}
