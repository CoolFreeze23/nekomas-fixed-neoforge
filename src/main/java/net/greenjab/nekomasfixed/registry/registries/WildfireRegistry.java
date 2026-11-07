package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.entity.WildFire.FireBomb;
import net.greenjab.nekomasfixed.registry.entity.WildFire.WildfireAttackablesSensor;
import net.greenjab.nekomasfixed.registry.entity.WildFire.WildfireEntity;
import net.greenjab.nekomasfixed.registry.entity.WildfireTrident;
import net.greenjab.nekomasfixed.registry.item.NetherHeartItem;
import net.greenjab.nekomasfixed.registry.item.WildfireShieldItem;
import net.greenjab.nekomasfixed.registry.item.WildfireTridentItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/** The Wildfire boss, its projectiles and the gear made from what it drops. */
public class WildfireRegistry {
    public static final SensorType<WildfireAttackablesSensor> WILDFIRE_ATTACK_ENTITY_SENSOR = Registry.register(
            BuiltInRegistries.SENSOR_TYPE, NekomasFixed.id("wildfire_attack_entity_sensor"), new SensorType<>(WildfireAttackablesSensor::new));

    public static final EntityType<WildfireTrident> WILDFIRE_TRIDENT_ENTITY = registerEntity("wildfire_trident",
            EntityType.Builder.<WildfireTrident>of(WildfireTrident::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20));

    public static final EntityType<FireBomb> FIRE_BOMB = registerEntity("fire_bomb",
            EntityType.Builder.<FireBomb>of(FireBomb::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

    public static final EntityType<WildfireEntity> WILDFIRE = registerEntity("wildfire",
            EntityType.Builder.of(WildfireEntity::new, MobCategory.MONSTER).fireImmune().sized(0.75F, 1.975F).clientTrackingRange(8));

    // White tints leave the egg's own texture as it is.
    public static final Item WILDFIRE_SPAWN_EGG = registerItem("wildfire_spawn_egg",
            new SpawnEggItem(WILDFIRE, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));
    public static final Item NETHER_HEART = registerItem("nether_heart", new NetherHeartItem());
    public static final Item WILDFIRE_TRIDENT = registerItem("wildfire_trident", new WildfireTridentItem(new Item.Properties()
            .rarity(Rarity.RARE).durability(1000).attributes(WildfireTridentItem.createAttributeModifiers())
            .component(DataComponents.TOOL, WildfireTridentItem.createToolComponent()).fireResistant()));
    public static final Item WILDFIRE_SHIELD = registerItem("wildfire_shield", new WildfireShieldItem(new Item.Properties()
            .rarity(Rarity.RARE).durability(336).fireResistant()));

    private static <T extends Entity> EntityType<T> registerEntity(String id, EntityType.Builder<T> builder) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE,
                ResourceKey.create(Registries.ENTITY_TYPE, NekomasFixed.id(id)),
                builder.build(id));
    }

    private static Item registerItem(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, NekomasFixed.id(id), item);
    }

    public static void register() {
        DispenserBlock.registerProjectileBehavior(WILDFIRE_TRIDENT);
        ItemGroupRegistry.MAIN_EXTRAS.add(WILDFIRE_SPAWN_EGG);
        ItemGroupRegistry.MAIN_EXTRAS.add(NETHER_HEART);
        ItemGroupRegistry.MAIN_EXTRAS.add(WILDFIRE_TRIDENT);
        ItemGroupRegistry.MAIN_EXTRAS.add(WILDFIRE_SHIELD);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WILDFIRE, WildfireEntity.createWildfireAttributes().build());
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(WILDFIRE, SpawnPlacementTypes.IN_LAVA, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WildfireEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** Smithing templates build their own item properties, so these come in through the component event. */
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modify(NETHER_HEART, builder -> builder.set(DataComponents.RARITY, Rarity.UNCOMMON).set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE));
    }
}
