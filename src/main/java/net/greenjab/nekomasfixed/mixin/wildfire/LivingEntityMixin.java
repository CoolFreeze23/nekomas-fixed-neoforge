package net.greenjab.nekomasfixed.mixin.wildfire;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.greenjab.nekomasfixed.registry.entity.WildFire.WildfireEntity;
import net.greenjab.nekomasfixed.registry.item.WildfireShieldItem;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyExpressionValue(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean wildFireSecondPhase(boolean original, @Local(argsOnly = true) DamageSource source) {
        LivingEntity LE = (LivingEntity)(Object)this;
        if (LE instanceof WildfireEntity wildFireEntity) {
            if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                return false;
            } else {
                if (!wildFireEntity.isSoulActive()) {
                    wildFireEntity.setSoulActive(true);
                    wildFireEntity.setShieldsActive(4);
                    wildFireEntity.setHealth(wildFireEntity.getMaxHealth());
                    wildFireEntity.level().broadcastEntityEvent(wildFireEntity, EntityEvent.TALISMAN_ACTIVATE);
                    return true;
                }
            }
        }
        return original;
    }

    @Inject(method = "blockUsingShield", at = @At("HEAD"))
    private void onShieldHit(LivingEntity attacker, CallbackInfo ci) {
        LivingEntity defender = (LivingEntity)(Object)this;
        if (defender.getUseItem().getItem() instanceof WildfireShieldItem && defender instanceof Player player) {
            if (player.getHealth() <= 6.0f) {
                attacker.igniteForTicks(20 * 3);
                attacker.knockback(1.0, attacker.getX() + player.getX(), attacker.getZ() + player.getZ());
            } else {
                attacker.igniteForTicks(20);
            }
        }
    }
}
