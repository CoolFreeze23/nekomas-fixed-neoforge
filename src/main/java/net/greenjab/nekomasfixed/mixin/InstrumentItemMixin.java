package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.block.enums.GoatHornType;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InstrumentItem.class)
public class InstrumentItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    public void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (level.isClientSide()) return;
        ItemStack itemStack = player.getItemInHand(hand);
        Holder<Instrument> instrument = itemStack.get(DataComponents.INSTRUMENT);
        if (instrument != null) {
            ServerLevel serverWorld = (ServerLevel) level;
            for (Entity entity : serverWorld.getAllEntities()) {
                if (entity instanceof IronGolem ironGolem) {
                    if (player.hasEffect(MobEffects.RAID_OMEN) || player.hasEffect(MobEffects.BAD_OMEN))
                        ironGolem.addEffect(new MobEffectInstance(GoatHornType.fromInstrument(instrument).getStatusEffect()));
                }
                if (entity instanceof TamableAnimal tameable && tameable.isTame()) tameable.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30 * 20));
                if (entity instanceof AbstractHorse horse && horse.isTamed()) horse.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30 * 20));
            }
        }
    }
}
