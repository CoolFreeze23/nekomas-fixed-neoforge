package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.registries.CrownRegistry;
import net.greenjab.nekomasfixed.registry.registries.TrimRegistry;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class SmithingTemplateDamageMixin {

    // The wildfire's templates survive explosions, 1.21.1 has no damage_resistant component for it.
    @Inject(method = "canBeHurtBy", at = @At("HEAD"), cancellable = true)
    private void templatesSurviveExplosions(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if ((stack.is(CrownRegistry.CROWN_SMITHING_TEMPLATE) || stack.is(TrimRegistry.JEWEL_ARMOR_TRIM_SMITHING_TEMPLATE))
                && source.is(DamageTypeTags.IS_EXPLOSION)) {
            cir.setReturnValue(false);
        }
    }
}
