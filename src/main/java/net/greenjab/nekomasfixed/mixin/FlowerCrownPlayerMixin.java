package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.item.FlowerCrownItem;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Player.class)
public abstract class FlowerCrownPlayerMixin {

    @Unique
    private static final Random FLOWER_CROWN_RANDOM = new Random();

    // A hit on someone wearing a flower crown can give them the crown's effect. A feather only knocks back.
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void applyFlowerCrownEffect(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.level().isClientSide() || player.getMainHandItem().is(Items.FEATHER)) return;
        if (!(target instanceof LivingEntity livingTarget)) return;
        ItemStack headItem = livingTarget.getItemBySlot(EquipmentSlot.HEAD);
        if (!(headItem.getItem() instanceof FlowerCrownItem crown)) return;
        if (FLOWER_CROWN_RANDOM.nextFloat() >= 0.5f) return;

        Holder<MobEffect> effect = crown.getVariant().effect.effect();
        livingTarget.addEffect(new MobEffectInstance(effect, 20 * 5, 0, false, true, true));
    }
}
