package net.greenjab.nekomasfixed.registry.other;

import com.mojang.serialization.Codec;
import net.greenjab.nekomasfixed.registry.block.enums.SpriteFacing;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record PotEngravingDecoration(Optional<Item> left, Optional<Item> right, Optional<Item> back, Optional<Item> front) {
    public static final PotEngravingDecoration EMPTY = new PotEngravingDecoration(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    public static final Item FALLBACK_CONSTANT = Items.AIR;
    public static final Codec<PotEngravingDecoration> CODEC = BuiltInRegistries.ITEM.byNameCodec().sizeLimitedListOf(4)
            .xmap(PotEngravingDecoration::new, PotEngravingDecoration::ordered);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotEngravingDecoration> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM)
            .apply(ByteBufCodecs.list(4))
            .map(PotEngravingDecoration::new, PotEngravingDecoration::ordered);

    public PotEngravingDecoration(List<Item> items) {
        this(getItem(items, 0), getItem(items, 1), getItem(items, 2), getItem(items, 3));
    }

    public PotEngravingDecoration removeEngraving(String side) {
        return switch (side.toLowerCase()) {
            case "left" -> new PotEngravingDecoration(Optional.empty(), this.right, this.back, this.front);
            case "right" -> new PotEngravingDecoration(this.left, Optional.empty(), this.back, this.front);
            case "back" -> new PotEngravingDecoration(this.left, this.right, Optional.empty(), this.front);
            case "front" -> new PotEngravingDecoration(this.left, this.right, this.back, Optional.empty());
            default -> this;
        };
    }

    public Item getRight() {
        return this.right.orElse(FALLBACK_CONSTANT);
    }

    public Item getLeft() {
        return this.left.orElse(FALLBACK_CONSTANT);
    }

    public Item getFront() {
        return this.front.orElse(FALLBACK_CONSTANT);
    }

    public Item getBack() {
        return this.back.orElse(FALLBACK_CONSTANT);
    }

    public PotEngravingDecoration engraveSideFacing(Direction hitDir, Direction potFacing, Item item) {
        String side = getSideName(hitDir, potFacing);
        if (side == null) return this;
        return switch (side) {
            case "left" -> new PotEngravingDecoration(Optional.of(item), this.right, this.back, this.front);
            case "right" -> new PotEngravingDecoration(this.left, Optional.of(item), this.back, this.front);
            case "back" -> new PotEngravingDecoration(this.left, this.right, Optional.of(item), this.front);
            case "front" -> new PotEngravingDecoration(this.left, this.right, this.back, Optional.of(item));
            default -> this;
        };
    }

    public Item getItemEngravedAt(Direction hitDir, Direction potFacing) {
        String side = getSideName(hitDir, potFacing);
        if (side == null) return Items.BRICK;
        return switch (side) {
            case "left" -> this.getLeft();
            case "right" -> this.getRight();
            case "back" -> this.getBack();
            case "front" -> this.getFront();
            default -> Items.BRICK;
        };
    }

    private static String getSideName(Direction hitDir, Direction potFacing) {
        if (hitDir.getAxis().isVertical()) return null;
        if (hitDir == potFacing) return "front";
        if (hitDir == potFacing.getOpposite()) return "back";
        if (hitDir == potFacing.getCounterClockWise()) return "left";
        if (hitDir == potFacing.getClockWise()) return "right";
        return null;
    }

    public List<Item> ordered() {
        return Stream.of(this.left, this.right, this.back, this.front)
                .map(item -> item.orElse(FALLBACK_CONSTANT))
                .toList();
    }

    public static Optional<Item> getItem(final List<Item> items, final int i) {
        if (i >= items.size()) {
            return Optional.empty();
        }
        Item item = items.get(i);
        return Optional.ofNullable(item).filter(it -> it != Items.BRICK && it != FALLBACK_CONSTANT);
    }

    public Item getDecoration(SpriteFacing facing) {
        return switch (facing) {
            case FRONT -> this.getFront();
            case BACK -> this.getBack();
            case LEFT -> this.getLeft();
            case RIGHT -> this.getRight();
        };
    }
}
