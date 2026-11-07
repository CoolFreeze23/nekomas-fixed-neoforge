package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.TermitehiveBlock;
import net.greenjab.nekomasfixed.registry.block.entity.TermitehiveBlockEntity;
import net.greenjab.nekomasfixed.registry.entity.Termite;
import net.greenjab.nekomasfixed.registry.other.TermitesComponent;
import net.greenjab.nekomasfixed.registry.worldgen.ModFeatures;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;

public class TermiteRegistry {

    public static final EntityDataSerializer<Termite.State> TERMITE_STATE = EntityDataSerializer.forValueType(Termite.State.PACKET_CODEC);

    public static final DataComponentType<TermitesComponent> TERMITES = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, NekomasFixed.id("termites"),
            DataComponentType.<TermitesComponent>builder()
                    .persistent(TermitesComponent.CODEC)
                    .networkSynchronized(TermitesComponent.PACKET_CODEC)
                    .cacheEncoding()
                    .build());

    public static final Block TERMITE_BLOCK = Registry.register(BuiltInRegistries.BLOCK, NekomasFixed.id("termite_block"),
            new Block(BlockBehaviour.Properties.of().strength(1f)));
    public static final Block TERMITE_HIVE = Registry.register(BuiltInRegistries.BLOCK, NekomasFixed.id("termite_hive"),
            new TermitehiveBlock(BlockBehaviour.Properties.of().strength(1f)));

    @SuppressWarnings("DataFlowIssue")
    public static final BlockEntityType<TermitehiveBlockEntity> TERMITE_HIVE_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, NekomasFixed.id("termite_hive"),
            new BlockEntityType<>(TermitehiveBlockEntity::new, Set.of(TERMITE_HIVE), null));

    public static final EntityType<Termite> TERMITE = Registry.register(BuiltInRegistries.ENTITY_TYPE, NekomasFixed.id("termite"),
            EntityType.Builder.of(Termite::new, MobCategory.MONSTER).sized(0.5f, 0.5f).build("termite"));

    public static final Item TERMITE_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("termite_spawn_egg"),
            new SpawnEggItem(TERMITE, 0x8B5A2B, 0xD9B38C, new Item.Properties()));
    public static final Item TERMITE_HIVE_ITEM = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("termite_hive"),
            new BlockItem(TERMITE_HIVE, new Item.Properties()));
    public static final Item TERMITE_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id("termite_block"),
            new BlockItem(TERMITE_BLOCK, new Item.Properties()));

    public static void register() {
        Registry.register(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, NekomasFixed.id("termite_state"), TERMITE_STATE);
        ModFeatures.register();
        ItemGroupRegistry.MAIN_EXTRAS.add(TERMITE_SPAWN_EGG);
        ItemGroupRegistry.MAIN_EXTRAS.add(TERMITE_HIVE_ITEM);
        ItemGroupRegistry.MAIN_EXTRAS.add(TERMITE_BLOCK_ITEM);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TERMITE, Termite.createAttributes().build());
    }
}
