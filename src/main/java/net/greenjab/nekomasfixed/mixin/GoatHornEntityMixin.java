package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.util.ModTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class GoatHornEntityMixin {

    @Unique
    private float nekomasfixed$goatHornCooldown = 80f;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickGoatHornCooldown(CallbackInfo info) {
        nekomasfixed$goatHornCooldown -= nekomasfixed$goatHornCooldown <= 0 ? 0 : 1;
    }

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void throwEntityWhenWearingGoatHornHelmet(Player player, CallbackInfo ci) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModTags.GOAT_HORN_HELMETS)) {
            Entity entity = (Entity) (Object) this;

            if (entity instanceof LivingEntity && player.getXRot() > 40 && nekomasfixed$goatHornCooldown <= 0) {
                entity.push(player.getLookAngle().scale(3d).add(0, 1.5, 0));
                player.getItemBySlot(EquipmentSlot.HEAD).hurtAndBreak(1, player, EquipmentSlot.HEAD);
                nekomasfixed$goatHornCooldown = 80f;
            }
        }
    }
}
