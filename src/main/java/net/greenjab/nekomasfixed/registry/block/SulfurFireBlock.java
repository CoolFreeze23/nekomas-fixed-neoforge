package net.greenjab.nekomasfixed.registry.block;

import com.mojang.serialization.MapCodec;
import net.greenjab.nekomasfixed.registry.registries.SulfurRegistry;
import net.greenjab.nekomasfixed.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SulfurFireBlock extends BaseFireBlock {
    public static final MapCodec<SulfurFireBlock> CODEC = simpleCodec(SulfurFireBlock::new);
    private final ParticleOptions PARTICLE_SMOKE = ParticleTypes.LARGE_SMOKE;

    public SulfurFireBlock(Properties properties) {
        super(properties, 1.5f);
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction directionToNeighbour, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return this.canSurvive(state, level, pos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSurviveOnBlock(level.getBlockState(pos.below()));
    }

    public static boolean canSurviveOnBlock(BlockState state) {
        return state.is(ModTags.SULFUR_BLOCKS);
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, true, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
        }
        for (int i = 0; i < 3; ++i) {
            double xx = pos.getX() + random.nextDouble();
            double yy = pos.getY() + random.nextDouble() * 0.5 + 1.0;
            double zz = pos.getZ() + random.nextDouble();
            level.addParticle(PARTICLE_SMOKE, xx, yy, zz, 0.0, 0.0, 0.0);
            if (i < 1) level.addParticle(SulfurRegistry.NOXIOUS_GAS, xx, yy, zz, 0.0, 0.0, 0.0);
        }
    }
}
