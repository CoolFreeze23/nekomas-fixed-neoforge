package net.greenjab.nekomasfixed.registry.recipe;

import net.greenjab.nekomasfixed.registry.registries.KilnRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;

public class KilnRecipe extends AbstractCookingRecipe {
    public static final RecipeSerializer<KilnRecipe> SERIALIZER = new SimpleCookingSerializer<>(KilnRecipe::new, 100);

    public KilnRecipe(String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(KilnRegistry.KILN_RECIPE, group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(KilnRegistry.KILN_ITEM);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
