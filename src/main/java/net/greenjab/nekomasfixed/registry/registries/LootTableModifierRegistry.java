package net.greenjab.nekomasfixed.registry.registries;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * For making modifications to vanilla loot tables
 * (in order not prevent conflicts with overriding same loot table with other mods)
 */
public class LootTableModifierRegistry {
    private static Set<ResourceKey<LootTable>> bannerTables;

    private static Set<ResourceKey<LootTable>> bannerTables() {
        if (bannerTables == null) {
            bannerTables = new HashSet<>();
            for (Block block : BuiltInRegistries.BLOCK) {
                if (block instanceof AbstractBannerBlock) {
                    bannerTables.add(block.getLootTable());
                }
            }
        }
        return bannerTables;
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, event.getName());
        DataComponentType<?> component;
        if (key.equals(Blocks.DECORATED_POT.getLootTable())) {
            component = ComponentRegistry.SHERD_GLOW_OVERRIDES;
        } else if (bannerTables().contains(key)) {
            component = ComponentRegistry.BANNER_EFFECTS;
        } else {
            return;
        }

        var enchantment = event.getRegistries().lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(Enchantments.SILK_TOUCH));
        if (enchantment.isEmpty()) return;
        Holder<Enchantment> silkTouch = enchantment.get();

        LootItemFunction function = CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                .include(component)
                .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                        .withSubPredicate(ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.Enchantments.enchantments(
                                List.of(new EnchantmentPredicate(silkTouch, Ints.atLeast(1)))))))
                .build();

        LootTable table = event.getTable();
        List<LootItemFunction> functions = new ArrayList<>(table.functions);
        functions.add(function);
        event.setTable(new LootTable(table.getParamSet(), table.randomSequence, table.pools, functions));
    }
}
