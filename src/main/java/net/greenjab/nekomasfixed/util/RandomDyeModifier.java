package net.greenjab.nekomasfixed.util;

import com.mojang.serialization.MapCodec;
import net.greenjab.nekomasfixed.registry.item.ModDyeItems;
import net.greenjab.nekomasfixed.registry.registries.BrushRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.List;

public class RandomDyeModifier implements LootItemFunction {
    public static final MapCodec<RandomDyeModifier> CODEC = MapCodec.unit(RandomDyeModifier::new);

    @Override
    public LootItemFunctionType<RandomDyeModifier> getType() {
        return BrushRegistry.RANDOM_DYE;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        List<Item> allDyes = BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof DyeItem || item instanceof ModDyeItems)
                .toList();

        if (!allDyes.isEmpty()) {
            RandomSource random = context.getRandom();
            return new ItemStack(allDyes.get(random.nextInt(allDyes.size())), stack.getCount());
        }

        return stack;
    }

    public static LootItemFunction.Builder builder() {
        return RandomDyeModifier::new;
    }
}
