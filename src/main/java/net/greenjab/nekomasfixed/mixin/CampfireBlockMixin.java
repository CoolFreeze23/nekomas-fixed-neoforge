package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.registries.SulfurRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    /** A sulfur campfire gives off gas in place of smoke, rising faster over a signal fire like the vanilla signal smoke. */
    @Inject(method = "makeParticles", at = @At("HEAD"), cancellable = true)
    private static void makeParticles(Level level, BlockPos pos, boolean isSignalFire, boolean spawnExtraSmoke, CallbackInfo ci) {
        if (!level.getBlockState(pos).is(SulfurRegistry.SULFUR_CAMPFIRE)) return;
        RandomSource random = level.getRandom();
        level.addAlwaysVisibleParticle(
                SulfurRegistry.NOXIOUS_GAS,
                true,
                pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                pos.getY() + random.nextDouble() + random.nextDouble(),
                pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                0.0,
                isSignalFire ? 0.14 : 0.07,
                0.0);
        if (spawnExtraSmoke) {
            level.addParticle(
                    SulfurRegistry.NOXIOUS_GAS,
                    pos.getX() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
                    pos.getY() + 0.4,
                    pos.getZ() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
                    0.0,
                    0.005,
                    0.0);
        }
        ci.cancel();
    }
}
