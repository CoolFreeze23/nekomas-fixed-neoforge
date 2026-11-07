package net.greenjab.nekomasfixed.registry.block.entity;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.greenjab.nekomasfixed.registry.other.TermitesComponent;
import net.greenjab.nekomasfixed.registry.registries.TermiteRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TermitehiveBlockEntity extends BlockEntity {
    static final Logger LOGGER = LogUtils.getLogger();
    static final List<String> IRRELEVANT_TERMITE_NBT_KEYS = Arrays.asList(
            "Air",
            "ArmorDropChances",
            "ArmorItems",
            "Brain",
            "CanPickUpLoot",
            "DeathTime",
            "FallDistance",
            "FallFlying",
            "Fire",
            "HandDropChances",
            "HandItems",
            "HurtByTimestamp",
            "HurtTime",
            "LeftHanded",
            "Motion",
            "NoGravity",
            "OnGround",
            "PortalCooldown",
            "Pos",
            "Rotation",
            "SleepingX",
            "SleepingY",
            "SleepingZ",
            "CannotEnterHiveTicks",
            "TicksSincePollination",
            "CropsGrownSincePollination",
            "hive_pos",
            "Passengers",
            "leash",
            "UUID"
    );
    private final List<TermitehiveBlockEntity.Termite> termites = Lists.newArrayList();

    public TermitehiveBlockEntity(BlockPos pos, BlockState state) {
        super(TermiteRegistry.TERMITE_HIVE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void setChanged() {
        this.angerTermites(TermitehiveBlockEntity.TermiteState.EMERGENCY);
        super.setChanged();
    }

    public boolean hasNoTermites() {
        return this.termites.isEmpty();
    }

    public boolean isFullOfTermites() {
        return this.termites.size() == 2;
    }

    public void angerTermites(TermitehiveBlockEntity.TermiteState termiteState) {
        if (this.level == null) return;
        List<Entity> list = Lists.newArrayList();
        this.termites.removeIf(termite -> releaseTermite(this.level, this.worldPosition, termite.createData(), list, termiteState));
        if (!list.isEmpty()) {
            super.setChanged();
        }
    }

    @VisibleForDebug
    public int getTermiteCount() {
        return this.termites.size();
    }

    public void tryEnterMound(net.greenjab.nekomasfixed.registry.entity.Termite entity) {
        if (this.termites.size() < 2) {
            entity.stopRiding();
            entity.ejectPassengers();
            entity.dropLeash(true, true);
            this.addTermite(TermitehiveBlockEntity.TermiteData.of(entity));
            if (this.level != null) {

                BlockPos blockPos = this.getBlockPos();
                this.level
                        .playSound(
                                null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F
                        );
                this.level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, this.getBlockState()));
            }

            entity.discard();
            super.setChanged();
        }
    }

    public void addTermite(TermitehiveBlockEntity.TermiteData termite) {
        this.termites.add(new TermitehiveBlockEntity.Termite(termite));
    }

    private static boolean releaseTermite(
            Level level,
            BlockPos pos,
            TermitehiveBlockEntity.TermiteData termite,
            @Nullable List<Entity> entities,
            TermitehiveBlockEntity.TermiteState termiteState
    ) {

        Direction direction = Direction.fromYRot(level.getRandom().nextInt(360));
        BlockPos blockPos = pos.relative(direction);
        boolean bl = !level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty();
        if (bl && termiteState != TermitehiveBlockEntity.TermiteState.EMERGENCY) {
            return false;
        } else {
            Entity entity = termite.loadEntity(level);
            if (entity != null) {
                if (entity instanceof net.greenjab.nekomasfixed.registry.entity.Termite termiteEntity) {

                    if (entities != null) {
                        entities.add(termiteEntity);
                    }

                    float f = entity.getBbWidth();
                    double d = bl ? 0.0 : 0.55 + f / 2.0F;
                    double e = pos.getX() + 0.5 + d * direction.getStepX();
                    double g = pos.getY() + 0.5 - entity.getBbHeight() / 2.0F;
                    double h = pos.getZ() + 0.5 + d * direction.getStepZ();
                    entity.moveTo(e, g, h, entity.getYRot(), entity.getXRot());
                }

                level.playSound(null, pos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, level.getBlockState(pos)));
                return level.addFreshEntity(entity);
            } else {
                return false;
            }
        }
    }

    private static void tickTermites(Level level, BlockPos pos, BlockState state, List<TermitehiveBlockEntity.Termite> termites) {
        boolean bl = false;
        Iterator<TermitehiveBlockEntity.Termite> iterator = termites.iterator();

        while (iterator.hasNext()) {
            TermitehiveBlockEntity.Termite termite = iterator.next();
            if (termite.canExitHive()) {
                TermitehiveBlockEntity.TermiteState termiteState = TermitehiveBlockEntity.TermiteState.TERMITE_RELEASED;
                if (releaseTermite(level, pos, termite.createData(), null, termiteState)) {
                    bl = true;
                    iterator.remove();
                }
            }
        }

        if (bl) {
            setChanged(level, pos, state);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TermitehiveBlockEntity blockEntity) {
        tickTermites(level, pos, state, blockEntity.termites);
        if (!blockEntity.termites.isEmpty() && level.getRandom().nextDouble() < 0.005) {
            double d = pos.getX() + 0.5;
            double e = pos.getY();
            double f = pos.getZ() + 0.5;
            level.playSound(null, d, e, f, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    protected void loadAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.termites.clear();
        if (tag.contains("termites")) {
            TermitehiveBlockEntity.TermiteData.LIST_CODEC
                    .parse(NbtOps.INSTANCE, tag.get("termites"))
                    .resultOrPartial(message -> LOGGER.error("Failed to parse termites: '{}'", message))
                    .ifPresent(list -> list.forEach(this::addTermite));
        }
    }

    @Override
    protected void saveAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("termites", TermitehiveBlockEntity.TermiteData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.createTermitesData()).getOrThrow());
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.@NonNull DataComponentInput components) {
        super.applyImplicitComponents(components);
        this.termites.clear();
        List<TermitehiveBlockEntity.TermiteData> list = components.getOrDefault(TermiteRegistry.TERMITES, TermitesComponent.DEFAULT).termites();
        list.forEach(this::addTermite);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NonNull Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(TermiteRegistry.TERMITES, new TermitesComponent(this.createTermitesData()));
    }

    @Override
    public void removeComponentsFromTag(@NonNull CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("termites");
    }

    private List<TermitehiveBlockEntity.TermiteData> createTermitesData() {
        return this.termites.stream().map(TermitehiveBlockEntity.Termite::createData).toList();
    }

    static class Termite {
        private final TermitehiveBlockEntity.TermiteData data;
        private int ticksInHive;

        Termite(TermitehiveBlockEntity.TermiteData data) {
            this.data = data;
            this.ticksInHive = data.ticksInHive();
        }

        public boolean canExitHive() {
            return this.ticksInHive++ > this.data.minTicksInHive;
        }

        public TermitehiveBlockEntity.TermiteData createData() {
            return new TermitehiveBlockEntity.TermiteData(this.data.entityData, this.ticksInHive, this.data.minTicksInHive);
        }

    }

    public record TermiteData(CustomData entityData, int ticksInHive, int minTicksInHive) {
        public static final Codec<TermitehiveBlockEntity.TermiteData> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                CustomData.CODEC.fieldOf("entity_data").forGetter(TermitehiveBlockEntity.TermiteData::entityData),
                                Codec.INT.fieldOf("ticks_in_hive").forGetter(TermitehiveBlockEntity.TermiteData::ticksInHive),
                                Codec.INT.fieldOf("min_ticks_in_hive").forGetter(TermitehiveBlockEntity.TermiteData::minTicksInHive)
                        )
                        .apply(instance, TermitehiveBlockEntity.TermiteData::new)
        );
        public static final Codec<List<TermitehiveBlockEntity.TermiteData>> LIST_CODEC = CODEC.listOf();
        public static final StreamCodec<ByteBuf, TermitehiveBlockEntity.TermiteData> PACKET_CODEC = StreamCodec.composite(
                CustomData.STREAM_CODEC,
                TermitehiveBlockEntity.TermiteData::entityData,
                ByteBufCodecs.VAR_INT,
                TermitehiveBlockEntity.TermiteData::ticksInHive,
                ByteBufCodecs.VAR_INT,
                TermitehiveBlockEntity.TermiteData::minTicksInHive,
                TermitehiveBlockEntity.TermiteData::new
        );

        public static TermitehiveBlockEntity.TermiteData of(Entity entity) {
            CompoundTag nbtCompound = new CompoundTag();
            entity.save(nbtCompound);
            TermitehiveBlockEntity.IRRELEVANT_TERMITE_NBT_KEYS.forEach(nbtCompound::remove);
            boolean bl = nbtCompound.getBoolean("HasNectar");
            return new TermitehiveBlockEntity.TermiteData(CustomData.of(nbtCompound), 0, bl ? 2400 : 600);
        }

        public static TermitehiveBlockEntity.TermiteData create(int ticksInHive) {
            CompoundTag nbtCompound = new CompoundTag();
            nbtCompound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(TermiteRegistry.TERMITE).toString());
            return new TermitehiveBlockEntity.TermiteData(CustomData.of(nbtCompound), ticksInHive, 600);
        }

        @Nullable
        public Entity loadEntity(Level level) {
            CompoundTag nbtCompound = this.entityData.copyTag();
            TermitehiveBlockEntity.IRRELEVANT_TERMITE_NBT_KEYS.forEach(nbtCompound::remove);
            Entity entity = EntityType.loadEntityRecursive(nbtCompound, level, loaded -> loaded);
            if (entity != null && entity.getType() == TermiteRegistry.TERMITE) {
                return entity;
            } else {
                return null;
            }
        }
    }

    public enum TermiteState {
        TERMITE_RELEASED,
        EMERGENCY
    }
}
