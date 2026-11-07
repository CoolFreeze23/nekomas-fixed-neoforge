package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.item.CrownItem;
import net.greenjab.nekomasfixed.registry.item.FlowerCrownItem;
import net.greenjab.nekomasfixed.util.FlowerCrownVariants;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class CrownRegistry {

    // 1.21.1 has no copper armour, so the copper crown carries that tier's numbers itself.
    public static final Holder<ArmorMaterial> COPPER_CROWN_MATERIAL = registerCopperMaterial();

    public static final Item CROWN_SMITHING_TEMPLATE = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("crown_smithing_template"),
            new SmithingTemplateItem(
                    Component.translatable(Util.makeDescriptionId("item", NekomasFixed.id("helmets"))).withStyle(ChatFormatting.BLUE),
                    Component.translatable(Util.makeDescriptionId("item", NekomasFixed.id("nether_heart"))).withStyle(ChatFormatting.BLUE),
                    Component.translatable(Util.makeDescriptionId("item", NekomasFixed.id("crown_smithing_template"))).withStyle(ChatFormatting.GRAY),
                    Component.empty(),
                    Component.empty(),
                    List.of(ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet")),
                    List.of(NekomasFixed.id("item/empty_slot_nether_heart"))));

    public static final Item COPPER_CROWN = registerCrown("copper_crown", COPPER_CROWN_MATERIAL, 11, new Item.Properties());
    public static final Item IRON_CROWN = registerCrown("iron_crown", ArmorMaterials.IRON, 15, new Item.Properties());
    public static final Item GOLDEN_CROWN = registerCrown("golden_crown", ArmorMaterials.GOLD, 7, new Item.Properties());
    public static final Item DIAMOND_CROWN = registerCrown("diamond_crown", ArmorMaterials.DIAMOND, 33, new Item.Properties());
    public static final Item NETHERITE_CROWN = registerCrown("netherite_crown", ArmorMaterials.NETHERITE, 37, new Item.Properties().fireResistant());

    public static final List<Item> FLOWER_CROWNS = new ArrayList<>();
    public static final Item TORCHFLOWER_FLOWER_CROWN = registerFlowerCrown("torchflower_flower_crown", FlowerCrownVariants.TORCHFLOWER);
    public static final Item BLUE_ORCHID_FLOWER_CROWN = registerFlowerCrown("blue_orchid_flower_crown", FlowerCrownVariants.BLUE_ORCHID);
    public static final Item WITHER_ROSE_FLOWER_CROWN = registerFlowerCrown("wither_rose_flower_crown", FlowerCrownVariants.WITHER_ROSE);
    public static final Item CORNFLOWER_FLOWER_CROWN = registerFlowerCrown("cornflower_flower_crown", FlowerCrownVariants.CORNFLOWER);
    public static final Item LILY_OF_THE_VALLEY_FLOWER_CROWN = registerFlowerCrown("lily_of_the_valley_flower_crown", FlowerCrownVariants.LILY_OF_THE_VALLEY);
    public static final Item ORANGE_TULIP_FLOWER_CROWN = registerFlowerCrown("orange_tulip_flower_crown", FlowerCrownVariants.ORANGE_TULIP);
    public static final Item PINK_TULIP_FLOWER_CROWN = registerFlowerCrown("pink_tulip_flower_crown", FlowerCrownVariants.PINK_TULIP);
    public static final Item ALLIUM_FLOWER_CROWN = registerFlowerCrown("allium_flower_crown", FlowerCrownVariants.ALLIUM);
    public static final Item RED_TULIP_FLOWER_CROWN = registerFlowerCrown("red_tulip_flower_crown", FlowerCrownVariants.RED_TULIP);
    public static final Item POPPY_FLOWER_CROWN = registerFlowerCrown("poppy_flower_crown", FlowerCrownVariants.POPPY);
    public static final Item AZURE_BLUET_FLOWER_CROWN = registerFlowerCrown("azure_bluet_flower_crown", FlowerCrownVariants.AZURE_BLUET);
    public static final Item WHITE_TULIP_FLOWER_CROWN = registerFlowerCrown("white_tulip_flower_crown", FlowerCrownVariants.WHITE_TULIP);
    public static final Item OXEYE_DAISY_FLOWER_CROWN = registerFlowerCrown("oxeye_daisy_flower_crown", FlowerCrownVariants.OXEYE_DAISY);
    public static final Item DANDELION_FLOWER_CROWN = registerFlowerCrown("dandelion_flower_crown", FlowerCrownVariants.DANDELION);

    private static Holder<ArmorMaterial> registerCopperMaterial() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 1);
        defense.put(ArmorItem.Type.LEGGINGS, 3);
        defense.put(ArmorItem.Type.CHESTPLATE, 4);
        defense.put(ArmorItem.Type.HELMET, 2);
        defense.put(ArmorItem.Type.BODY, 4);
        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, NekomasFixed.id("copper_crown"),
                new ArmorMaterial(
                        defense,
                        8,
                        SoundEvents.ARMOR_EQUIP_IRON,
                        () -> Ingredient.of(Items.COPPER_INGOT),
                        List.of(new ArmorMaterial.Layer(NekomasFixed.id("copper_crown"))),
                        0.0F, 0.0F));
    }

    private static Item registerCrown(String id, Holder<ArmorMaterial> material, int durability, Item.Properties properties) {
        return Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id(id),
                new CrownItem(material, id, properties.durability(ArmorItem.Type.HELMET.getDurability(durability))));
    }

    private static Item registerFlowerCrown(String id, FlowerCrownVariants variant) {
        Item item = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id(id),
                new FlowerCrownItem(new Item.Properties().stacksTo(1), variant));
        FLOWER_CROWNS.add(item);
        return item;
    }

    public static void register() {
        ItemGroupRegistry.MAIN_EXTRAS.add(TrimRegistry.JEWEL_ARMOR_TRIM_SMITHING_TEMPLATE);
        ItemGroupRegistry.MAIN_EXTRAS.add(CROWN_SMITHING_TEMPLATE);
        ItemGroupRegistry.MAIN_EXTRAS.add(COPPER_CROWN);
        ItemGroupRegistry.MAIN_EXTRAS.add(IRON_CROWN);
        ItemGroupRegistry.MAIN_EXTRAS.add(GOLDEN_CROWN);
        ItemGroupRegistry.MAIN_EXTRAS.add(DIAMOND_CROWN);
        ItemGroupRegistry.MAIN_EXTRAS.add(NETHERITE_CROWN);
        ItemGroupRegistry.MAIN_EXTRAS.addAll(FLOWER_CROWNS);
    }

    /** The template constructors take no item properties in 1.21.1. */
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modify(CROWN_SMITHING_TEMPLATE, builder -> builder.set(DataComponents.RARITY, Rarity.UNCOMMON).set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE));
        event.modify(TrimRegistry.JEWEL_ARMOR_TRIM_SMITHING_TEMPLATE, builder -> builder.set(DataComponents.RARITY, Rarity.UNCOMMON).set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE));
    }
}
