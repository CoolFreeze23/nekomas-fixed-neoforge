package net.greenjab.nekomasfixed.registry.block.entity;

import net.greenjab.nekomasfixed.registry.other.PotEngravingDecoration;
import net.greenjab.nekomasfixed.registry.other.PotFaceDecoration;
import net.greenjab.nekomasfixed.registry.registries.ComponentRegistry;
import net.greenjab.nekomasfixed.registry.registries.PotRegistry;
import net.greenjab.nekomasfixed.target_access_class.DecoratedPotAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TerracottaDecoratedPotBlockEntity extends BlockEntity implements ContainerSingleItem.BlockContainerSingleItem, RandomizableContainer, DecoratedPotAccess {
    public long wobbleStartedAtTick;
    @Nullable
    public WobbleStyle lastWobbleStyle;
    private PotDecorations decorations = PotDecorations.EMPTY;
    public PotEngravingDecoration engravingDecorations = PotEngravingDecoration.EMPTY;
    public PotFaceDecoration faceDecoration = PotFaceDecoration.FALLBACK_BRICKS;
    private List<Boolean> sherdGlow = new ArrayList<>(ComponentRegistry.DEFAULT_SHERD_GLOW_OVERRIDES);
    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    protected ResourceKey<LootTable> lootTable;
    protected long lootTableSeed;

    public TerracottaDecoratedPotBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(PotRegistry.TERRACOTTA_DECORATED_POT_BLOCK_ENTITY, worldPosition, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.decorations.save(tag);
        if (!this.trySaveLootTable(tag) && !this.item.isEmpty()) {
            tag.put("item", this.item.save(registries));
        }
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        if (!this.engravingDecorations.equals(PotEngravingDecoration.EMPTY)) {
            PotEngravingDecoration.CODEC.encodeStart(ops, this.engravingDecorations).ifSuccess(value -> tag.put("engraved", value));
        }
        PotFaceDecoration.CODEC.encodeStart(ops, this.faceDecoration).ifSuccess(value -> tag.put("pot_face", value));
        if (this.sherdGlow.contains(true)) {
            tag.putByteArray(SHERD_GLOW_OVERRIDES_KEY, this.sherdGlow.stream().map(b -> b ? (byte) 1 : (byte) 0).toList());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.decorations = PotDecorations.load(tag);
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        this.engravingDecorations = tag.contains("engraved")
                ? PotEngravingDecoration.CODEC.parse(ops, tag.get("engraved")).result().orElse(PotEngravingDecoration.EMPTY)
                : PotEngravingDecoration.EMPTY;
        this.faceDecoration = tag.contains("pot_face")
                ? PotFaceDecoration.CODEC.parse(ops, tag.get("pot_face")).result().orElse(PotFaceDecoration.FALLBACK_BRICKS)
                : PotFaceDecoration.FALLBACK_BRICKS;
        if (!this.tryLoadLootTable(tag)) {
            this.item = tag.contains("item", CompoundTag.TAG_COMPOUND)
                    ? ItemStack.parse(registries, tag.getCompound("item")).orElse(ItemStack.EMPTY)
                    : ItemStack.EMPTY;
        }
        this.sherdGlow = new ArrayList<>(ComponentRegistry.DEFAULT_SHERD_GLOW_OVERRIDES);
        if (tag.contains(SHERD_GLOW_OVERRIDES_KEY, CompoundTag.TAG_BYTE_ARRAY)) {
            byte[] overrides = tag.getByteArray(SHERD_GLOW_OVERRIDES_KEY);
            for (int i = 0; i < 4 && i < overrides.length; i++) {
                this.sherdGlow.set(i, overrides[i] != 0);
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public PotDecorations getDecorations() {
        return this.decorations;
    }

    public PotEngravingDecoration getEngravingDecorations() {
        return this.engravingDecorations;
    }

    public PotFaceDecoration getPotFace() {
        return this.faceDecoration;
    }

    public static ItemStack createDecoratedPotInstance(PotDecorations decorations, PotEngravingDecoration engravingDecorations, PotFaceDecoration potFaceDecoration) {
        ItemStack stack = new ItemStack(PotRegistry.TERRACOTTA_DECORATED_POT_ITEM);
        stack.set(DataComponents.POT_DECORATIONS, decorations);
        stack.set(PotRegistry.POT_ENGRAVED_DECORATIONS, engravingDecorations);
        stack.set(PotRegistry.POT_FACE_BLOCK, potFaceDecoration);
        return stack;
    }

    @Nullable
    @Override
    public ResourceKey<LootTable> getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.POT_DECORATIONS, this.decorations);
        components.set(PotRegistry.POT_ENGRAVED_DECORATIONS, this.engravingDecorations);
        components.set(PotRegistry.POT_FACE_BLOCK, this.faceDecoration);
        components.set(ComponentRegistry.SHERD_GLOW_OVERRIDES, List.copyOf(this.sherdGlow));
        components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(this.item)));
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput components) {
        super.applyImplicitComponents(components);
        this.decorations = components.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY);
        this.engravingDecorations = components.getOrDefault(PotRegistry.POT_ENGRAVED_DECORATIONS, PotEngravingDecoration.EMPTY);
        this.faceDecoration = components.getOrDefault(PotRegistry.POT_FACE_BLOCK, PotFaceDecoration.FALLBACK_BRICKS);
        this.sherdGlow = new ArrayList<>(components.getOrDefault(ComponentRegistry.SHERD_GLOW_OVERRIDES, ComponentRegistry.DEFAULT_SHERD_GLOW_OVERRIDES));
        this.item = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne();
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("sherds");
        tag.remove("item");
        tag.remove("engraved");
        tag.remove("pot_face");
        tag.remove(SHERD_GLOW_OVERRIDES_KEY);
    }

    @Override
    public ItemStack getTheItem() {
        this.unpackLootTable(null);
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int count) {
        this.unpackLootTable(null);
        ItemStack result = this.item.split(count);
        if (this.item.isEmpty()) {
            this.item = ItemStack.EMPTY;
        }
        return result;
    }

    @Override
    public void setTheItem(ItemStack itemStack) {
        this.unpackLootTable(null);
        this.item = itemStack;
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public boolean nekomasfixed$getSherdGlow(int index) {
        return this.sherdGlow.get(index);
    }

    @Override
    public void nekomasfixed$setSherdGlow(int index, boolean glowing) {
        this.sherdGlow.set(index, glowing);
    }

    public void wobble(WobbleStyle wobbleStyle) {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, wobbleStyle.ordinal());
        }
    }

    @Override
    public boolean triggerEvent(int event, int data) {
        if (this.level != null && event == 1 && data >= 0 && data < WobbleStyle.values().length) {
            this.wobbleStartedAtTick = this.level.getGameTime();
            this.lastWobbleStyle = WobbleStyle.values()[data];
            return true;
        }
        return super.triggerEvent(event, data);
    }

    public enum WobbleStyle {
        POSITIVE(7),
        NEGATIVE(10);

        public final int duration;

        WobbleStyle(int duration) {
            this.duration = duration;
        }
    }
}
