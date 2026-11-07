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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkStarFadeRecipe.class)
public class FireworkStarFadeRecipeMixin {
    @Shadow
    @Final
    private static Ingredient STAR_INGREDIENT;

    @WrapOperation(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
            constant = @Constant(classValue = DyeItem.class))
    private boolean acceptExtraDyes(Object item, Operation<Boolean> original) {
        return original.call(item) || item instanceof ModDyeItems;
    }

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void craftCustom(CraftingInput input, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> cir) {
        IntList colours = new IntArrayList();
        ItemStack star = null;
        boolean extra = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            Item item = stack.getItem();
            if (item instanceof DyeItem dye) {
                colours.add(dye.getDyeColor().getFireworkColor());
            } else if (item instanceof ModDyeItems) {
                colours.add(ModColors.fireworkColor(item));
                extra = true;
            } else if (STAR_INGREDIENT.test(stack)) {
                star = stack.copyWithCount(1);
            }
        }
        if (extra && star != null) {
            star.update(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT, colours, FireworkExplosion::withFadeColors);
            cir.setReturnValue(star);
        }
    }
}
