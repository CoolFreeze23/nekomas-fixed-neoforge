package net.greenjab.nekomasfixed.registry.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Vanilla loot this mod adds to, in place of overriding whole tables. */
public class LootTableAdditions {

    public static void onLootTableLoad(LootTableLoadEvent event) {
        var key = event.getKey();
        if (key.equals(BuiltInLootTables.STRONGHOLD_LIBRARY)) {
            var enchantments = event.getRegistries().lookupOrThrow(Registries.ENCHANTMENT);
            enchantments.get(EnchantmentRegistry.LEECHING).ifPresent(leeching ->
                    event.getTable().addPool(LootPool.lootPool().name("nekomasfixed:leeching_book")
                            .add(LootItem.lootTableItem(Items.BOOK).apply(new EnchantRandomlyFunction.Builder().withEnchantment(leeching)))
                            .add(LootItem.lootTableItem(Items.AIR)).build()));
        }
    }

    /** An enderman blown up by a charged creeper drops its head, like the vanilla mob heads. */
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof EnderMan enderman
                && event.getSource().getEntity() instanceof Creeper creeper && creeper.canDropMobsSkull()) {
            creeper.increaseDroppedSkulls();
            var drop = new net.minecraft.world.entity.item.ItemEntity(enderman.level(), enderman.getX(), enderman.getY(), enderman.getZ(),
                    new ItemStack(ItemRegistry.ENDERMAN_HEAD));
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        }
    }
}
