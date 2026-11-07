package net.greenjab.nekomasfixed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.greenjab.nekomasfixed.util.options.RecipeOption;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Recipes that a world option turned off aren't found by crafting, lookups or the recipe book. */
@Mixin(RecipeManager.class)
public class RecipeOptionMixin {

    @Unique
    private static <R extends RecipeHolder<?>> Collection<R> nekomasfixed$filter(Collection<R> recipes) {
        boolean anyOff = false;
        for (RecipeOption option : RecipeOption.ALL_RECIPES) {
            if (!option.get()) {
                anyOff = true;
                break;
            }
        }
        if (!anyOff) return recipes;
        List<R> kept = recipes.stream().filter(holder -> ModConfigValues.isRecipeEnabled(holder.id())).toList();
        return kept;
    }

    @ModifyReturnValue(method = "byType", at = @At("RETURN"))
    private <T extends RecipeHolder<?>> Collection<T> filterByType(Collection<T> original) {
        return nekomasfixed$filter(original);
    }

    @ModifyReturnValue(method = "getRecipes", at = @At("RETURN"))
    private Collection<RecipeHolder<?>> filterAll(Collection<RecipeHolder<?>> original) {
        return nekomasfixed$filter(original);
    }

    @ModifyReturnValue(method = "byKey", at = @At("RETURN"))
    private Optional<RecipeHolder<?>> filterByKey(Optional<RecipeHolder<?>> original) {
        return original.filter(holder -> ModConfigValues.isRecipeEnabled(holder.id()));
    }

    @ModifyReturnValue(method = "byKeyTyped", at = @At("RETURN"))
    private <T extends RecipeHolder<?>> T filterByKeyTyped(T original) {
        return original != null && !ModConfigValues.isRecipeEnabled(original.id()) ? null : original;
    }
}
