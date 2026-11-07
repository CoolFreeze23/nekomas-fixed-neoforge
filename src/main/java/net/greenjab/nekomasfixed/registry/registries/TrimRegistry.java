package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.armortrim.TrimPattern;

public class TrimRegistry {
    public static final ResourceKey<TrimPattern> JEWEL =
            ResourceKey.create(Registries.TRIM_PATTERN, NekomasFixed.id("jewel"));

    public static final Item JEWEL_ARMOR_TRIM_SMITHING_TEMPLATE = Registry.register(BuiltInRegistries.ITEM,
            NekomasFixed.id("jewel_armor_trim_smithing_template"), SmithingTemplateItem.createArmorTrimTemplate(JEWEL));

    public static void register() {
        NekomasFixed.LOGGER.info("Registering trim patterns");
    }
}
