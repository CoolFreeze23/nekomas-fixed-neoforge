package net.greenjab.nekomasfixed.registry.other;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.List;
import java.util.function.Consumer;

public record TippedArrowCustomComponent(List<PotionContents> potionContents) implements TooltipProvider {
    public static final TippedArrowCustomComponent EMPTY = new TippedArrowCustomComponent(List.of(PotionContents.EMPTY));

    public static final MapCodec<TippedArrowCustomComponent> FULL_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PotionContents.CODEC.listOf()
                    .optionalFieldOf("potion_contents", List.of())
                    .forGetter(TippedArrowCustomComponent::potionContents)
    ).apply(i, TippedArrowCustomComponent::new));

    public static final Codec<TippedArrowCustomComponent> CODEC = FULL_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, TippedArrowCustomComponent> STREAM_CODEC =
            PotionContents.STREAM_CODEC
                    .apply(ByteBufCodecs.list())
                    .map(TippedArrowCustomComponent::new, TippedArrowCustomComponent::potionContents);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag) {
        for (PotionContents potion : potionContents) {
            consumer.accept(Component.literal(getName(potion)).withColor(potion.getColor()));
        }
    }

    public int[] getColorsForPotions() {
        return this.potionContents.stream().mapToInt(PotionContents::getColor).toArray();
    }

    private String getName(PotionContents potion) {
        String name = Potion.getName(potion.potion(), "").replace('_', ' ');
        return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
