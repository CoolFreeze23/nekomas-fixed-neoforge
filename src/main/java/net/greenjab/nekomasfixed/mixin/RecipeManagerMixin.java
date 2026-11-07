package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "byKey", at = @At("RETURN"), cancellable = true)
    private void nekomasfixed$filterByKey(ResourceLocation recipeId, CallbackInfoReturnable<Optional<RecipeHolder<?>>> cir) {
        if (!ModConfigValues.isRecipeEnabled(recipeId)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
