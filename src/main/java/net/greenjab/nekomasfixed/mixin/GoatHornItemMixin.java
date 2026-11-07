package net.greenjab.nekomasfixed.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class GoatHornItemMixin {

    // Blowing a goat horn inside a village puffs smoke around the player.
    @Inject(method = "onUseTick", at = @At("HEAD"))
    private void customUsageTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining, CallbackInfo ci) {
        if (level.isClientSide() || !itemStack.is(Items.GOAT_HORN)) return;

        if (livingEntity.tickCount % 3 == 0) {
            ServerLevel serverLevel = (ServerLevel) level;
            if (serverLevel.structureManager().getStructureWithPieceAt(livingEntity.blockPosition(), StructureTags.VILLAGE).isValid()) {
                serverLevel.sendParticles(ParticleTypes.POOF, livingEntity.getX(), livingEntity.getY() + 0.5, livingEntity.getZ(),
                        20, 0.4, 0.2, 0.4, 0.05);
            }
        }
    }
}
