package net.greenjab.nekomasfixed.registry.registries;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;
import java.util.Optional;

public class VillagerRegistry {
    public static final ResourceKey<PoiType> PYROTECHNIST_POI_KEY =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, NekomasFixed.id("pyrotechnist_poi"));

    public static final PoiType PYROTECHNIST_POI = Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, PYROTECHNIST_POI_KEY,
            new PoiType(ImmutableSet.copyOf(BlockRegistry.PYROTECHNICS_TABLE.getStateDefinition().getPossibleStates()), 1, 1));

    public static final VillagerProfession PYROTECHNIST = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, NekomasFixed.id("pyrotechnist"),
            new VillagerProfession("pyrotechnist", holder -> holder.is(PYROTECHNIST_POI_KEY), holder -> holder.is(PYROTECHNIST_POI_KEY),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_WEAPONSMITH));

    public static void registerVillagers() {
        NekomasFixed.LOGGER.info("Registering villagers: {}", PYROTECHNIST);
    }

    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != PYROTECHNIST) return;
        var trades = event.getTrades();

        trades.get(1).add(wants(Items.GUNPOWDER, 4, 10, 2, 0.05f));
        trades.get(1).add(wants(Items.GOLD_NUGGET, 27, 10, 4, 0.05f));
        trades.get(1).add(sells(Items.FIRE_CHARGE, 1, 4, 10, 2, 0.05f));
        trades.get(1).add(wants(Items.FEATHER, 24, 10, 2, 0.05f));

        trades.get(2).add(wants(Items.PAPER, 24, 16, 10, 0.05f));
        trades.get(2).add(sells(Items.GLOWSTONE_DUST, 1, 1, 12, 10, 0.05f));
        trades.get(2).add(wants(Items.DIAMOND, 1, 12, 10, 0.05f));
        trades.get(2).add(sells(Items.FLINT_AND_STEEL, 1, 1, 12, 10, 0.2f));
        trades.get(2).add(sells(ItemRegistry.REDSTONE_STRIKER, 1, 1, 12, 10, 0.2f));

        trades.get(3).add((trader, random) -> offer(2, randomFirework(new ItemStack(Items.FIREWORK_ROCKET), random), 12, 10, 0.05f));
        trades.get(3).add((trader, random) -> offer(1, randomFirework(new ItemStack(Items.FIREWORK_STAR), random), 12, 10, 0.05f));
        trades.get(3).add(sells(Items.CREEPER_BANNER_PATTERN, 8, 1, 12, 15, 0.05f));
        trades.get(3).add((trader, random) -> offer(1, randomDye(random), 12, 10, 0.05f));

        trades.get(4).add(wants(Items.BLAZE_ROD, 3, 12, 15, 0.05f));
        trades.get(4).add(sells(Items.CROSSBOW, 3, 1, 12, 15, 0.2f));
        trades.get(4).add(sells(Items.REDSTONE_TORCH, 1, 8, 12, 15, 0.05f));
        trades.get(4).add(wants(Items.COAL_BLOCK, 2, 12, 15, 0.05f));

        trades.get(5).add(sells(Items.TNT, 4, 1, 12, 20, 0.05f));
        trades.get(5).add(sells(Items.TNT_MINECART, 7, 1, 8, 20, 0.05f));
        trades.get(5).add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 12), Optional.of(new ItemCost(Items.CROSSBOW)),
                loadedCrossbow(trader, random), 6, 30, 0.2f));
        trades.get(5).add(sells(Items.WIND_CHARGE, 2, 4, 8, 30, 0.05f));
    }

    private static VillagerTrades.ItemListing wants(Item item, int count, int uses, int xp, float multiplier) {
        return new BasicItemListing(new ItemStack(item, count), new ItemStack(Items.EMERALD), uses, xp, multiplier);
    }

    private static VillagerTrades.ItemListing sells(Item item, int emeralds, int count, int uses, int xp, float multiplier) {
        return new BasicItemListing(new ItemStack(Items.EMERALD, emeralds), new ItemStack(item, count), uses, xp, multiplier);
    }

    private static MerchantOffer offer(int emeralds, ItemStack result, int uses, int xp, float multiplier) {
        return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), result, uses, xp, multiplier);
    }

    private static ItemStack randomFirework(ItemStack stack, RandomSource random) {
        FireworkExplosion.Shape[] shapes = FireworkExplosion.Shape.values();
        FireworkExplosion explosion = new FireworkExplosion(shapes[random.nextInt(shapes.length)],
                IntArrayList.of(random.nextInt(0xFFFFFF)), IntList.of(), random.nextBoolean(), random.nextBoolean());
        if (stack.is(Items.FIREWORK_STAR)) {
            stack.set(DataComponents.FIREWORK_EXPLOSION, explosion);
        } else {
            stack.set(DataComponents.FIREWORKS, new Fireworks(1 + random.nextInt(3), List.of(explosion)));
        }
        return stack;
    }

    private static ItemStack randomDye(RandomSource random) {
        List<Item> dyes = BuiltInRegistries.ITEM.stream().filter(DyeItem.class::isInstance).toList();
        return new ItemStack(dyes.get(random.nextInt(dyes.size())), 4);
    }

    private static ItemStack loadedCrossbow(net.minecraft.world.entity.Entity trader, RandomSource random) {
        FireworkExplosion explosion = new FireworkExplosion(FireworkExplosion.Shape.values()[random.nextInt(FireworkExplosion.Shape.values().length)],
                IntArrayList.of(random.nextInt(0xFFFFFF), random.nextInt(0xFFFFFF)), IntList.of(), true, true);
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        rocket.set(DataComponents.FIREWORKS, new Fireworks(3, List.of(explosion)));
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(rocket));
        var key = random.nextBoolean() ? Enchantments.QUICK_CHARGE : Enchantments.MULTISHOT;
        trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(key).ifPresent(holder -> {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(holder, key.equals(Enchantments.QUICK_CHARGE) ? 2 : 1);
            crossbow.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        });
        return crossbow;
    }
}
