package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.entity.CustomArrow;
import net.greenjab.nekomasfixed.registry.item.CustomArrowItem;
import net.greenjab.nekomasfixed.registry.item.quiver.QuiverContents;
import net.greenjab.nekomasfixed.registry.item.quiver.QuiverItem;
import net.greenjab.nekomasfixed.registry.other.TippedArrowCustomComponent;
import net.greenjab.nekomasfixed.registry.recipe.CustomTippedArrowRecipe;
import net.greenjab.nekomasfixed.util.LoadedCrossbowModifier;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

/** The quiver, the tipped arrows that hold several potions, and the loaded crossbow loot function. */
public class ArcheryRegistry {
    public static final DataComponentType<QuiverContents> QUIVER_CONTENTS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, NekomasFixed.id("quiver_contents"),
            DataComponentType.<QuiverContents>builder().persistent(QuiverContents.CODEC).networkSynchronized(QuiverContents.STREAM_CODEC).cacheEncoding().build());

    public static final DataComponentType<TippedArrowCustomComponent> TIPPED_POTION_CONTENTS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, NekomasFixed.id("tipped_potion_contents"),
            DataComponentType.<TippedArrowCustomComponent>builder().persistent(TippedArrowCustomComponent.CODEC).networkSynchronized(TippedArrowCustomComponent.STREAM_CODEC).cacheEncoding().build());

    public static final Item TIPPED_ARROW_CUSTOM = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("tipped_arrow_custom"),
            new CustomArrowItem(new Item.Properties().stacksTo(64).component(TIPPED_POTION_CONTENTS, TippedArrowCustomComponent.EMPTY)));

    public static final Item QUIVER = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("quiver"),
            new QuiverItem(new Item.Properties().stacksTo(1).component(QUIVER_CONTENTS, QuiverContents.EMPTY)));

    public static final EntityType<CustomArrow> CUSTOM_ARROW = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ResourceKey.create(Registries.ENTITY_TYPE, NekomasFixed.id("custom_arrow")),
            EntityType.Builder.<CustomArrow>of(CustomArrow::new, MobCategory.MISC).sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20).build("custom_arrow"));

    public static final RecipeType<CustomTippedArrowRecipe> CUSTOM_TIPPED_ARROW_RECIPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, NekomasFixed.id("tipped_custom_arrow"), new RecipeType<>() {
        @Override
        public String toString() {
            return "nekomasfixed:tipped_custom_arrow";
        }
    });

    public static final LootItemFunctionType<LoadedCrossbowModifier> LOADED_CROSSBOW = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, NekomasFixed.id("loaded_crossbow"),
            new LootItemFunctionType<>(LoadedCrossbowModifier.CODEC));

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, NekomasFixed.id("tipped_custom_arrow"), CustomTippedArrowRecipe.SERIALIZER);
        DataComponentPredicateRegistry.register();
        ItemGroupRegistry.MAIN_EXTRAS.add(QUIVER);
    }
}
