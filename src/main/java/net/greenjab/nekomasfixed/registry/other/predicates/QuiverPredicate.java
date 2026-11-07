package net.greenjab.nekomasfixed.registry.other.predicates;

import com.mojang.serialization.Codec;
import net.greenjab.nekomasfixed.registry.item.quiver.QuiverContents;
import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record QuiverPredicate() implements SingleComponentItemPredicate<QuiverContents> {
    public static final Codec<QuiverPredicate> CODEC = Codec.unit(new QuiverPredicate());

    @Override
    public DataComponentType<QuiverContents> componentType() {
        return ArcheryRegistry.QUIVER_CONTENTS;
    }

    @Override
    public boolean matches(final ItemStack stack, final QuiverContents contents) {
        return !contents.isEmpty();
    }
}
