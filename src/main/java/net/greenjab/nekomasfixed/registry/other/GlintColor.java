package net.greenjab.nekomasfixed.registry.other;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastColor;

import java.util.List;
import java.util.Optional;

public record GlintColor(List<Integer> colors, int cycleTicks, Component description, Optional<ResourceLocation> texture) {
    public static final ResourceKey<Registry<GlintColor>> REGISTRY_KEY = ResourceKey.createRegistryKey(NekomasFixed.id("glint"));

    private static final Codec<Integer> RGB_COLOR = Codec.STRING.comapFlatMap(GlintColor::parseColor, color -> String.format("#%06x", color & 0xFFFFFF));

    public static final Codec<GlintColor> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.nonEmptyList(RGB_COLOR.listOf()).fieldOf("colors").forGetter(GlintColor::colors),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("cycle_ticks", 80).forGetter(GlintColor::cycleTicks),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(GlintColor::description),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(GlintColor::texture)
    ).apply(i, GlintColor::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, GlintColor> DIRECT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), GlintColor::colors,
            ByteBufCodecs.INT, GlintColor::cycleTicks,
            ComponentSerialization.STREAM_CODEC, GlintColor::description,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), GlintColor::texture,
            GlintColor::new);
    public static final Codec<Holder<GlintColor>> CODEC = RegistryFileCodec.create(REGISTRY_KEY, DIRECT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<GlintColor>> STREAM_CODEC = ByteBufCodecs.holder(REGISTRY_KEY, DIRECT_STREAM_CODEC);

    private static DataResult<Integer> parseColor(String text) {
        if (text.length() != 7 || text.charAt(0) != '#') {
            return DataResult.error(() -> "Not an RGB color: " + text);
        }
        try {
            return DataResult.success(FastColor.ARGB32.opaque(Integer.parseInt(text.substring(1), 16)));
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Not an RGB color: " + text);
        }
    }

    public int colorAt(long timeMillis) {
        if (colors.size() == 1) {
            return colors.getFirst();
        }
        long cycleMillis = cycleTicks * 50L;
        long position = timeMillis % cycleMillis;
        int n = colors.size();
        int slots = Math.max(64, n * 8);
        int slot = (int) (position * slots / cycleMillis);
        float t = slot / (float) slots * n;
        int index = (int) t;
        float frac = t - index;
        int from = colors.get(index);
        int to = colors.get((index + 1) % n);
        return FastColor.ARGB32.lerp(frac, from, to);
    }
}
