package net.greenjab.nekomasfixed.registry.registries;

import com.mojang.serialization.Codec;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.other.predicates.QuiverPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class DataComponentPredicateRegistry {
    public static final ItemSubPredicate.Type<QuiverPredicate> HAS_ARROWS = register("has_arrows", QuiverPredicate.CODEC);

    private static <T extends ItemSubPredicate> ItemSubPredicate.Type<T> register(final String id, final Codec<T> codec) {
        return Registry.register(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, ResourceLocation.withDefaultNamespace(id), new ItemSubPredicate.Type<>(codec));
    }

    public static void register() {
        NekomasFixed.LOGGER.info("Registering item predicates");
    }
}
