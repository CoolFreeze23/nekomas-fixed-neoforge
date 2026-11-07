package net.greenjab.nekomasfixed.registry.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.greenjab.nekomasfixed.registry.other.TippedArrowCustomComponent;
import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class CustomTippedArrowRecipe extends CustomRecipe {
    public static final MapCodec<CustomTippedArrowRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CustomRecipe::category),
                    Ingredient.CODEC_NONEMPTY.fieldOf("arrow").forGetter(recipe -> recipe.arrow),
                    Ingredient.CODEC_NONEMPTY.fieldOf("potion").forGetter(recipe -> recipe.potion),
                    ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
            ).apply(instance, CustomTippedArrowRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CustomTippedArrowRecipe> STREAM_CODEC = StreamCodec.composite(
            CraftingBookCategory.STREAM_CODEC, CustomRecipe::category,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.arrow,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.potion,
            ItemStack.STREAM_CODEC, recipe -> recipe.result,
            CustomTippedArrowRecipe::new
    );

    public static final RecipeSerializer<CustomTippedArrowRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<CustomTippedArrowRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CustomTippedArrowRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    };

    private final Ingredient arrow;
    private final Ingredient potion;
    private final ItemStack result;

    public CustomTippedArrowRecipe(CraftingBookCategory category, Ingredient arrow, Ingredient potion, ItemStack result) {
        super(category);
        this.arrow = arrow;
        this.potion = potion;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean foundArrow = false;
        List<Holder<Potion>> potions = new ArrayList<>();

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;

            if (arrow.test(stack) && foundArrow) {
                return false;
            }

            if (arrow.test(stack)) {
                foundArrow = true;
                continue;
            }

            if (potion.test(stack)) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                if (contents == null) continue;

                Holder<Potion> type = contents.potion().orElse(null);
                if (type == null) continue;

                potions.add(type);
            }
        }

        if (!foundArrow || potions.isEmpty()) return false;

        for (int i = 0; i < potions.size(); i++) {
            for (int j = i + 1; j < potions.size(); j++) {
                if (!checkPotions(potions.get(i).value(), potions.get(j).value())) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkPotions(Potion pot1, Potion pot2) {
        List<MobEffectInstance> pot1Effects = pot1.getEffects();
        List<MobEffectInstance> pot2Effects = pot2.getEffects();

        if (pot1Effects.size() != pot2Effects.size()) {
            return true;
        }

        for (int i = 0; i < pot1Effects.size(); i++) {
            MobEffectInstance pot1Ins = pot1Effects.get(i);
            MobEffectInstance pot2Ins = pot2Effects.get(i);

            if (pot1Ins.getEffect().value().equals(pot2Ins.getEffect().value())) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<PotionContents> combinedEffects = new ArrayList<>();
        int count = 0;

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (arrow.test(stack) && count == 0) {
                count = stack.getCount();
            }
            if (potion.test(stack)) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                if (contents != null) {
                    combinedEffects.add(contents);
                }
            }
        }

        ItemStack output = result.copy();
        output.setCount(count);
        if (!combinedEffects.isEmpty()) {
            output.set(ArcheryRegistry.TIPPED_POTION_CONTENTS, new TippedArrowCustomComponent(combinedEffects));
        }

        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
