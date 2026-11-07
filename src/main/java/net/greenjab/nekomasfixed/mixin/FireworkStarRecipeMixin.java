package net.greenjab.nekomasfixed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.greenjab.nekomasfixed.registry.item.ModDyeItems;
import net.greenjab.nekomasfixed.util.ModColors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkStarRecipe.class)
public class FireworkStarRecipeMixin {

    @WrapOperation(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
            constant = @Constant(classValue = DyeItem.class))
    private boolean acceptExtraDyes(Object item, Operation<Boolean> original) {
        return original.call(item) || item instanceof ModDyeItems;
    }

    /** The vanilla loop only takes vanilla dyes: list the colours again in order with the extra dyes in. */
    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private void craftCustom(CraftingInput input, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> cir) {
        IntList colours = new IntArrayList();
        boolean extra = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof DyeItem dye) {
                colours.add(dye.getDyeColor().getFireworkColor());
            } else if (stack.getItem() instanceof ModDyeItems) {
                colours.add(ModColors.fireworkColor(stack.getItem()));
                extra = true;
            }
        }
        if (extra) {
            ItemStack star = cir.getReturnValue();
            FireworkExplosion explosion = star.get(DataComponents.FIREWORK_EXPLOSION);
            star.set(DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(explosion.shape(), colours, explosion.fadeColors(), explosion.hasTrail(), explosion.hasTwinkle()));
        }
    }
}
