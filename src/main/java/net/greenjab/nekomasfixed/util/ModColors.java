package net.greenjab.nekomasfixed.util;

import net.greenjab.nekomasfixed.registry.registries.ItemRegistry;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;

public class ModColors implements StringRepresentable {
    public static final ModColors AMBER = new ModColors("amber", 0xE0AF0B);
    public static final ModColors AQUA = new ModColors("aqua", 0xA6CEC7);
    public static final ModColors INDIGO = new ModColors("indigo", 0xFF453C8F);
    public static final ModColors MAROON = new ModColors("maroon", 0xFFA62D10);

    private final String name;
    private final int color;


    public ModColors(String name, int color){
        this.name = name;
        this.color = color;
    }

    public String getName(){
        return name;
    }

    public int getColor(){
        return color;
    }

    /** The firework colour of one of the four extra dyes, as plain RGB. */
    public static int fireworkColor(Item dye) {
        if (dye == ItemRegistry.AMBER_DYE) return AMBER.getColor();
        if (dye == ItemRegistry.AQUA_DYE) return AQUA.getColor();
        if (dye == ItemRegistry.INDIGO_DYE) return INDIGO.getColor();
        return MAROON.getColor();
    }

    @Override
    public @NonNull String getSerializedName() {
        return this.name;
    }
}