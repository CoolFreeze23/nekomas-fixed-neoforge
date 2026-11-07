package net.greenjab.nekomasfixed.util;

import net.minecraft.world.inventory.RecipeBookType;

/** The kiln's recipe book, a constant added to the vanilla enum through META-INF/enumextensions.json. */
public final class ModRecipeBookType {
    public static final RecipeBookType KILN = RecipeBookType.valueOf("NEKOMASFIXED_KILN");

    private ModRecipeBookType() {
    }
}
