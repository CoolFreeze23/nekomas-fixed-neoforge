package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.other.AnimalComponent;
import net.greenjab.nekomasfixed.registry.other.AnimalTooltipData;
import net.greenjab.nekomasfixed.registry.registries.ComponentRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class AnimalTooltipItemStackMixin {

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    private void useAnimalTooltip(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        AnimalComponent animal = stack.get(ComponentRegistry.ANIMAL);
        if (animal != null && !animal.animal().isEmpty() && !stack.has(DataComponents.HIDE_TOOLTIP)) {
            cir.setReturnValue(Optional.of(new AnimalTooltipData(animal)));
        }
    }
}
