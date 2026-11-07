package net.greenjab.nekomasfixed.registry.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.greenjab.nekomasfixed.registry.item.PrismItem;
import net.greenjab.nekomasfixed.registry.other.ArmorGlint;
import net.greenjab.nekomasfixed.registry.other.GlintColor;
import net.greenjab.nekomasfixed.registry.registries.GlintRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class ArmorGlintRecipe implements SmithingRecipe {
    public static final MapCodec<ArmorGlintRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("template").forGetter(r -> r.template),
            Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(r -> r.base),
            Ingredient.CODEC_NONEMPTY.fieldOf("addition").forGetter(r -> r.addition)
    ).apply(i, ArmorGlintRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorGlintRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.template,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.base,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.addition,
            ArmorGlintRecipe::new);

    public static final RecipeSerializer<ArmorGlintRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<ArmorGlintRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ArmorGlintRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    };

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    public ArmorGlintRecipe(Ingredient template, Ingredient base, Ingredient addition) {
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return template.test(input.template()) && base.test(input.base()) && addition.test(input.addition());
    }

    // The glint colours are data, so a prism's default component can't hold one: the item names its key instead.
    private static Holder<GlintColor> providedGlint(ItemStack template, HolderLookup.Provider registries) {
        Holder<GlintColor> color = template.get(GlintRegistry.PROVIDES_GLINT);
        if (color == null && template.getItem() instanceof PrismItem prism) {
            color = registries.lookup(GlintColor.REGISTRY_KEY).flatMap(lookup -> lookup.get(prism.glint())).orElse(null);
        }
        return color;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        Holder<GlintColor> color = providedGlint(input.template(), registries);
        if (color == null) return ItemStack.EMPTY;

        ArmorGlint glint = new ArmorGlint(color);
        if (glint.equals(input.base().get(GlintRegistry.ARMOR_GLINT))) return ItemStack.EMPTY; // already this color

        ItemStack result = input.base().copyWithCount(1);
        result.set(GlintRegistry.ARMOR_GLINT, glint);
        return result;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack[] items = this.base.getItems();
        return items.length == 0 ? ItemStack.EMPTY : items[0].copy();
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return addition.test(stack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
