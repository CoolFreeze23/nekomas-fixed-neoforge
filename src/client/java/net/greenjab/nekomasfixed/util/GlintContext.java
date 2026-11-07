package net.greenjab.nekomasfixed.util;

import net.greenjab.nekomasfixed.registry.other.ArmorGlint;
import net.greenjab.nekomasfixed.registry.registries.GlintRegistry;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** The glint of the item being drawn right now. A colour of 0 means the plain vanilla foil. */
public final class GlintContext {
    private static int color;
    private static ResourceLocation base;

    private GlintContext() {
    }

    public static int getColor() {
        return color;
    }

    public static ResourceLocation getBase() {
        return base;
    }

    public static void set(ItemStack stack) {
        ArmorGlint glint = stack.get(GlintRegistry.ARMOR_GLINT);
        if (glint == null) {
            clear();
        } else {
            color = glint.color().value().colorAt(Util.getMillis());
            base = glint.color().value().texture().orElse(null);
        }
    }

    public static void clear() {
        color = 0;
        base = null;
    }
}
