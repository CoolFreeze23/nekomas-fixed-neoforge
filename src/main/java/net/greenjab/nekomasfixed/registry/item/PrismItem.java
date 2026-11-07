package net.greenjab.nekomasfixed.registry.item;

import net.greenjab.nekomasfixed.registry.other.GlintColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/** A smithing template that gives armor a coloured glint. The colour lives in the glint data registry. */
public class PrismItem extends Item {
    private final ResourceKey<GlintColor> glint;

    public PrismItem(ResourceKey<GlintColor> glint, Properties properties) {
        super(properties);
        this.glint = glint;
    }

    public ResourceKey<GlintColor> glint() {
        return this.glint;
    }
}
