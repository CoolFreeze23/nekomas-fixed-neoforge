package net.greenjab.nekomasfixed.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.greenjab.nekomasfixed.registry.other.ArmorGlint;
import net.greenjab.nekomasfixed.registry.registries.GlintRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackGlintMixin {

    // Custom components aren't added to the tooltip on their own in 1.21.1.
    @Inject(method = "getTooltipLines",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", ordinal = 0))
    private void addGlintTooltip(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local Consumer<Component> consumer) {
        ArmorGlint glint = ((ItemStack) (Object) this).get(GlintRegistry.ARMOR_GLINT);
        if (glint != null) {
            glint.addToTooltip(context, consumer, flag);
        }
    }
}
