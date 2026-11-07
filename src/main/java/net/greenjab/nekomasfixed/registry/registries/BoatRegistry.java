package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.entity.BigBoat;
import net.greenjab.nekomasfixed.registry.entity.FakeBoat;
import net.greenjab.nekomasfixed.registry.entity.HugeBoat;
import net.greenjab.nekomasfixed.registry.item.BigBoatItem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.List;
import java.util.function.Supplier;

public class BoatRegistry {

    public static final EntityType<FakeBoat> FAKE_BOAT = register(
            "fake_boat", EntityType.Builder.of(FakeBoat::new, MobCategory.MISC)
                    .sized(1.65f, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));

    public static final EntityType<BigBoat> BIG_ACACIA_BOAT_TYPE = bigBoatType("big_acacia_boat", () -> BoatRegistry.BIG_ACACIA_BOAT, Boat.Type.ACACIA);
    public static final EntityType<BigBoat> BIG_BAMBOO_BOAT_TYPE = bigBoatType("big_bamboo_boat", () -> BoatRegistry.BIG_BAMBOO_BOAT, Boat.Type.BAMBOO);
    public static final EntityType<BigBoat> BIG_BIRCH_BOAT_TYPE = bigBoatType("big_birch_boat", () -> BoatRegistry.BIG_BIRCH_BOAT, Boat.Type.BIRCH);
    public static final EntityType<BigBoat> BIG_CHERRY_BOAT_TYPE = bigBoatType("big_cherry_boat", () -> BoatRegistry.BIG_CHERRY_BOAT, Boat.Type.CHERRY);
    public static final EntityType<BigBoat> BIG_DARK_OAK_BOAT_TYPE = bigBoatType("big_dark_oak_boat", () -> BoatRegistry.BIG_DARK_OAK_BOAT, Boat.Type.DARK_OAK);
    public static final EntityType<BigBoat> BIG_JUNGLE_BOAT_TYPE = bigBoatType("big_jungle_boat", () -> BoatRegistry.BIG_JUNGLE_BOAT, Boat.Type.JUNGLE);
    public static final EntityType<BigBoat> BIG_MANGROVE_BOAT_TYPE = bigBoatType("big_mangrove_boat", () -> BoatRegistry.BIG_MANGROVE_BOAT, Boat.Type.MANGROVE);
    public static final EntityType<BigBoat> BIG_OAK_BOAT_TYPE = bigBoatType("big_oak_boat", () -> BoatRegistry.BIG_OAK_BOAT, Boat.Type.OAK);
    public static final EntityType<BigBoat> BIG_SPRUCE_BOAT_TYPE = bigBoatType("big_spruce_boat", () -> BoatRegistry.BIG_SPRUCE_BOAT, Boat.Type.SPRUCE);
    public static final EntityType<BigBoat> BIG_BAOBAB_BOAT_TYPE = bigBoatType("big_baobab_boat", () -> BoatRegistry.BIG_BAOBAB_BOAT, Boat.Type.OAK);

    public static final EntityType<HugeBoat> HUGE_ACACIA_BOAT_TYPE = hugeBoatType("huge_acacia_boat", () -> BoatRegistry.HUGE_ACACIA_BOAT, Boat.Type.ACACIA);
    public static final EntityType<HugeBoat> HUGE_BAMBOO_BOAT_TYPE = hugeBoatType("huge_bamboo_boat", () -> BoatRegistry.HUGE_BAMBOO_BOAT, Boat.Type.BAMBOO);
    public static final EntityType<HugeBoat> HUGE_BIRCH_BOAT_TYPE = hugeBoatType("huge_birch_boat", () -> BoatRegistry.HUGE_BIRCH_BOAT, Boat.Type.BIRCH);
    public static final EntityType<HugeBoat> HUGE_CHERRY_BOAT_TYPE = hugeBoatType("huge_cherry_boat", () -> BoatRegistry.HUGE_CHERRY_BOAT, Boat.Type.CHERRY);
    public static final EntityType<HugeBoat> HUGE_DARK_OAK_BOAT_TYPE = hugeBoatType("huge_dark_oak_boat", () -> BoatRegistry.HUGE_DARK_OAK_BOAT, Boat.Type.DARK_OAK);
    public static final EntityType<HugeBoat> HUGE_JUNGLE_BOAT_TYPE = hugeBoatType("huge_jungle_boat", () -> BoatRegistry.HUGE_JUNGLE_BOAT, Boat.Type.JUNGLE);
    public static final EntityType<HugeBoat> HUGE_MANGROVE_BOAT_TYPE = hugeBoatType("huge_mangrove_boat", () -> BoatRegistry.HUGE_MANGROVE_BOAT, Boat.Type.MANGROVE);
    public static final EntityType<HugeBoat> HUGE_OAK_BOAT_TYPE = hugeBoatType("huge_oak_boat", () -> BoatRegistry.HUGE_OAK_BOAT, Boat.Type.OAK);
    public static final EntityType<HugeBoat> HUGE_SPRUCE_BOAT_TYPE = hugeBoatType("huge_spruce_boat", () -> BoatRegistry.HUGE_SPRUCE_BOAT, Boat.Type.SPRUCE);
    public static final EntityType<HugeBoat> HUGE_BAOBAB_BOAT_TYPE = hugeBoatType("huge_baobab_boat", () -> BoatRegistry.HUGE_BAOBAB_BOAT, Boat.Type.OAK);

    public static final List<EntityType<BigBoat>> bigBoats = List.of(BIG_ACACIA_BOAT_TYPE, BIG_BAMBOO_BOAT_TYPE, BIG_BIRCH_BOAT_TYPE, BIG_CHERRY_BOAT_TYPE, BIG_DARK_OAK_BOAT_TYPE, BIG_JUNGLE_BOAT_TYPE, BIG_MANGROVE_BOAT_TYPE, BIG_OAK_BOAT_TYPE, BIG_SPRUCE_BOAT_TYPE, BIG_BAOBAB_BOAT_TYPE);
    public static final List<EntityType<HugeBoat>> hugeBoats = List.of(HUGE_ACACIA_BOAT_TYPE, HUGE_BAMBOO_BOAT_TYPE, HUGE_BIRCH_BOAT_TYPE, HUGE_CHERRY_BOAT_TYPE, HUGE_DARK_OAK_BOAT_TYPE, HUGE_JUNGLE_BOAT_TYPE, HUGE_MANGROVE_BOAT_TYPE, HUGE_OAK_BOAT_TYPE, HUGE_SPRUCE_BOAT_TYPE, HUGE_BAOBAB_BOAT_TYPE);
    private static final List<Boat.Type> smallBoats = List.of(Boat.Type.ACACIA, Boat.Type.BAMBOO, Boat.Type.BIRCH, Boat.Type.CHERRY, Boat.Type.DARK_OAK, Boat.Type.JUNGLE, Boat.Type.MANGROVE, Boat.Type.OAK, Boat.Type.SPRUCE);

    public static final Item BOAT_UPGRADE_TEMPLATE = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("boat_upgrade_template"),
            new SmithingTemplateItem(Component.translatable(Util.makeDescriptionId("item", NekomasFixed.id("boat")))
                    .withStyle(ChatFormatting.BLUE), Component.translatable(Util.makeDescriptionId("item", NekomasFixed.id("planks")))
                    .withStyle(ChatFormatting.BLUE), Component.nullToEmpty(""), Component.nullToEmpty(""), Component.nullToEmpty(""), List.of(NekomasFixed.id("container/slot/boat")),
                    List.of(NekomasFixed.id("container/slot/planks"))));

    public static final Item BIG_OAK_BOAT = boatItem("big_oak_boat", BIG_OAK_BOAT_TYPE);
    public static final Item BIG_SPRUCE_BOAT = boatItem("big_spruce_boat", BIG_SPRUCE_BOAT_TYPE);
    public static final Item BIG_BIRCH_BOAT = boatItem("big_birch_boat", BIG_BIRCH_BOAT_TYPE);
    public static final Item BIG_JUNGLE_BOAT = boatItem("big_jungle_boat", BIG_JUNGLE_BOAT_TYPE);
    public static final Item BIG_ACACIA_BOAT = boatItem("big_acacia_boat", BIG_ACACIA_BOAT_TYPE);
    public static final Item BIG_DARK_OAK_BOAT = boatItem("big_dark_oak_boat", BIG_DARK_OAK_BOAT_TYPE);
    public static final Item BIG_MANGROVE_BOAT = boatItem("big_mangrove_boat", BIG_MANGROVE_BOAT_TYPE);
    public static final Item BIG_CHERRY_BOAT = boatItem("big_cherry_boat", BIG_CHERRY_BOAT_TYPE);
    public static final Item BIG_BAMBOO_BOAT = boatItem("big_bamboo_boat", BIG_BAMBOO_BOAT_TYPE);
    public static final Item BIG_BAOBAB_BOAT = boatItem("big_baobab_boat", BIG_BAOBAB_BOAT_TYPE);

    public static final Item HUGE_OAK_BOAT = boatItem("huge_oak_boat", HUGE_OAK_BOAT_TYPE);
    public static final Item HUGE_SPRUCE_BOAT = boatItem("huge_spruce_boat", HUGE_SPRUCE_BOAT_TYPE);
    public static final Item HUGE_BIRCH_BOAT = boatItem("huge_birch_boat", HUGE_BIRCH_BOAT_TYPE);
    public static final Item HUGE_JUNGLE_BOAT = boatItem("huge_jungle_boat", HUGE_JUNGLE_BOAT_TYPE);
    public static final Item HUGE_ACACIA_BOAT = boatItem("huge_acacia_boat", HUGE_ACACIA_BOAT_TYPE);
    public static final Item HUGE_DARK_OAK_BOAT = boatItem("huge_dark_oak_boat", HUGE_DARK_OAK_BOAT_TYPE);
    public static final Item HUGE_MANGROVE_BOAT = boatItem("huge_mangrove_boat", HUGE_MANGROVE_BOAT_TYPE);
    public static final Item HUGE_CHERRY_BOAT = boatItem("huge_cherry_boat", HUGE_CHERRY_BOAT_TYPE);
    public static final Item HUGE_BAMBOO_BOAT = boatItem("huge_bamboo_boat", HUGE_BAMBOO_BOAT_TYPE);
    public static final Item HUGE_BAOBAB_BOAT = boatItem("huge_baobab_boat", HUGE_BAOBAB_BOAT_TYPE);

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> builder) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE,
                ResourceKey.create(Registries.ENTITY_TYPE, NekomasFixed.id(id)),
                builder.build(id));
    }

    private static EntityType<BigBoat> bigBoatType(String id, Supplier<Item> item, Boat.Type variant) {
        return register(id, EntityType.Builder.<BigBoat>of((type, level) -> new BigBoat(type, level, item, variant), MobCategory.MISC)
                .sized(1.9f, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
    }

    private static EntityType<HugeBoat> hugeBoatType(String id, Supplier<Item> item, Boat.Type variant) {
        return register(id, EntityType.Builder.<HugeBoat>of((type, level) -> new HugeBoat(type, level, item, variant), MobCategory.MISC)
                .sized(2.6f, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
    }

    private static Item boatItem(String id, EntityType<? extends BigBoat> type) {
        return Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id(id), new BigBoatItem(type, new Item.Properties().stacksTo(1)));
    }

    /** A plain boat of the same wood as bigBoats.get(index), for the smaller ships of a patrol. */
    public static Boat smallBoat(int index, Level level) {
        if (index >= smallBoats.size()) return EntityTypeRegistry.BAOBAB_BOAT.create(level);
        Boat boat = EntityType.BOAT.create(level);
        if (boat != null) boat.setVariant(smallBoats.get(index));
        return boat;
    }

    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(BOAT_UPGRADE_TEMPLATE, builder -> builder.set(DataComponents.RARITY, Rarity.UNCOMMON));
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (BuiltInLootTables.SHIPWRECK_TREASURE.location().equals(event.getName())) {
            event.getTable().addPool(LootPool.lootPool()
                    .add(LootItem.lootTableItem(BOAT_UPGRADE_TEMPLATE))
                    .add(LootItem.lootTableItem(Items.AIR))
                    .build());
        }
    }

    public static void register() {
        ItemGroupRegistry.MAIN_EXTRAS.addAll(List.of(BOAT_UPGRADE_TEMPLATE,
                BIG_OAK_BOAT, BIG_SPRUCE_BOAT, BIG_BIRCH_BOAT, BIG_JUNGLE_BOAT, BIG_ACACIA_BOAT, BIG_DARK_OAK_BOAT, BIG_MANGROVE_BOAT, BIG_CHERRY_BOAT, BIG_BAMBOO_BOAT, BIG_BAOBAB_BOAT,
                HUGE_OAK_BOAT, HUGE_SPRUCE_BOAT, HUGE_BIRCH_BOAT, HUGE_JUNGLE_BOAT, HUGE_ACACIA_BOAT, HUGE_DARK_OAK_BOAT, HUGE_MANGROVE_BOAT, HUGE_CHERRY_BOAT, HUGE_BAMBOO_BOAT, HUGE_BAOBAB_BOAT));
    }
}
