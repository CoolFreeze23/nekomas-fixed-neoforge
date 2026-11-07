package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.entity.GoatHornBlockEntity;
import net.greenjab.nekomasfixed.registry.item.GoatHornHelmetItem;
import net.greenjab.nekomasfixed.util.GoatHornHelmetTiers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class GoatHornRegistry {

    @SuppressWarnings("DataFlowIssue")
    public static final BlockEntityType<GoatHornBlockEntity> GOAT_HORN_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, NekomasFixed.id("goat_horn"),
            new BlockEntityType<>(GoatHornBlockEntity::new, Set.of(BlockRegistry.GOAT_HORN), null));

    public static final Item GOAT_HORN_IRON_HELMET = registerGoatHornHelm("goat_horn_iron_helmet", GoatHornHelmetTiers.IRON);
    public static final Item GOAT_HORN_GOLDEN_HELMET = registerGoatHornHelm("goat_horn_golden_helmet", GoatHornHelmetTiers.GOLDEN);
    public static final Item GOAT_HORN_DIAMOND_HELMET = registerGoatHornHelm("goat_horn_diamond_helmet", GoatHornHelmetTiers.DIAMOND);
    public static final Item GOAT_HORN_NETHERITE_HELMET = registerGoatHornHelm("goat_horn_netherite_helmet", GoatHornHelmetTiers.NETHERITE);
    public static final Item GOAT_HORN_TURTLE_HELMET = registerGoatHornHelm("goat_horn_turtle_helmet", GoatHornHelmetTiers.TURTLE);

    private static Item registerGoatHornHelm(String id, GoatHornHelmetTiers tier) {
        Item.Properties props = new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(tier.getDurability()));
        if (tier == GoatHornHelmetTiers.NETHERITE) {
            props.fireResistant();
        }
        return Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id(id), new GoatHornHelmetItem(props, tier));
    }

    public static void register() {
        ItemGroupRegistry.MAIN_EXTRAS.add(GOAT_HORN_IRON_HELMET);
        ItemGroupRegistry.MAIN_EXTRAS.add(GOAT_HORN_GOLDEN_HELMET);
        ItemGroupRegistry.MAIN_EXTRAS.add(GOAT_HORN_DIAMOND_HELMET);
        ItemGroupRegistry.MAIN_EXTRAS.add(GOAT_HORN_NETHERITE_HELMET);
        ItemGroupRegistry.MAIN_EXTRAS.add(GOAT_HORN_TURTLE_HELMET);
    }
}
