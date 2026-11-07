package net.greenjab.nekomasfixed.registries;

import net.greenjab.nekomasfixed.registry.recipe.KilnRecipe;
import net.greenjab.nekomasfixed.registry.registries.KilnRegistry;
import net.greenjab.nekomasfixed.util.ModRecipeBookType;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;

import java.util.List;
import java.util.function.Supplier;

/** The kiln's recipe book tabs, constants added to the vanilla enum through META-INF/enumextensions.json. */
public final class ModRecipeBookCategories {
    public static final EnumProxy<RecipeBookCategories> KILN_SEARCH = new EnumProxy<>(RecipeBookCategories.class, (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Items.COMPASS)));
    public static final EnumProxy<RecipeBookCategories> KILN_BLOCKS = new EnumProxy<>(RecipeBookCategories.class, (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Items.SAND)));
    public static final EnumProxy<RecipeBookCategories> KILN_MISC = new EnumProxy<>(RecipeBookCategories.class, (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Items.CLAY_BALL)));

    private ModRecipeBookCategories() {
    }

    public static void register(RegisterRecipeBookCategoriesEvent event) {
        RecipeBookCategories search = KILN_SEARCH.getValue();
        RecipeBookCategories blocks = KILN_BLOCKS.getValue();
        RecipeBookCategories misc = KILN_MISC.getValue();
        event.registerAggregateCategory(search, List.of(blocks, misc));
        event.registerBookCategories(ModRecipeBookType.KILN, List.of(search, blocks, misc));
        event.registerRecipeCategoryFinder(KilnRegistry.KILN_RECIPE, holder ->
                holder.value() instanceof KilnRecipe recipe && recipe.category() == CookingBookCategory.BLOCKS ? blocks : misc);
    }
}
