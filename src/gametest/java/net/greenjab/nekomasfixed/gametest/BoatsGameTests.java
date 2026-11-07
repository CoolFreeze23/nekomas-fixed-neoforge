package net.greenjab.nekomasfixed.gametest;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.entity.BigBoat;
import net.greenjab.nekomasfixed.registry.entity.FakeBoat;
import net.greenjab.nekomasfixed.registry.entity.HugeBoat;
import net.greenjab.nekomasfixed.registry.registries.BoatRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.List;

@GameTestHolder(NekomasFixed.NAMESPACE)
@PrefixGameTestTemplate(false)
public class BoatsGameTests {
    private static final List<String> ALL_WOODS = List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo", "baobab");
    // Upstream main has smithing recipes for these. It has none for mangrove and baobab.
    private static final List<String> RECIPE_WOODS = List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "cherry", "bamboo");

    private static Item item(String id) {
        ResourceLocation key = id.contains(":") ? ResourceLocation.parse(id) : NekomasFixed.id(id);
        return BuiltInRegistries.ITEM.get(key);
    }

    private static Zombie rider(GameTestHelper h) {
        return h.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(1.5, 2, 1.5));
    }

    private static void assertSeat(GameTestHelper h, Entity boat, Entity rider, double forward, String name) {
        Vec3 expected = new Vec3(0, 0, forward).yRot(-boat.getYRot() * (float) (Math.PI / 180.0));
        Vec3 actual = rider.position().subtract(boat.position());
        h.assertTrue(Math.abs(actual.x - expected.x) < 0.06 && Math.abs(actual.z - expected.z) < 0.06,
                name + " sits at " + actual + ", expected " + expected);
        double height = actual.y + rider.getVehicleAttachmentPoint(boat).y;
        h.assertTrue(Math.abs(height - (0.5625 / 3.0 + 0.2)) < 0.02, name + " sits at height " + height);
    }

    private static void fillPool(GameTestHelper h) {
        for (int x = 0; x < 15; x++) {
            for (int z = 0; z < 15; z++) {
                boolean edge = x == 0 || z == 0 || x == 14 || z == 14;
                if (edge) {
                    h.setBlock(x, 2, z, Blocks.STONE);
                    h.setBlock(x, 3, z, Blocks.STONE);
                }
            }
        }
        for (int x = 1; x < 14; x++) {
            for (int z = 1; z < 14; z++) {
                h.setBlock(x, 2, z, Blocks.WATER);
            }
        }
    }

    private static List<FakeBoat> fakesAround(GameTestHelper h, Entity boat) {
        return h.getLevel().getEntitiesOfClass(FakeBoat.class, boat.getBoundingBox().inflate(4.0));
    }

    @GameTest(template = "platform")
    public static void everyWoodIsRegistered(GameTestHelper h) {
        for (String size : List.of("big", "huge")) {
            for (String wood : ALL_WOODS) {
                ResourceLocation id = NekomasFixed.id(size + "_" + wood + "_boat");
                h.assertTrue(BuiltInRegistries.ITEM.containsKey(id), "item " + id);
                h.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(id), "entity " + id);
                Entity entity = BuiltInRegistries.ENTITY_TYPE.get(id).create(h.getLevel());
                h.assertTrue(entity instanceof BigBoat, id + " is a big boat");
                h.assertTrue(((BigBoat) entity).getDropItem() == BuiltInRegistries.ITEM.get(id), id + " drops itself");
                h.assertTrue(size.equals("huge") == (entity instanceof HugeBoat), id + " size");
            }
        }
        h.assertTrue(BuiltInRegistries.ENTITY_TYPE.get(NekomasFixed.id("big_bamboo_boat")).create(h.getLevel()) instanceof BigBoat b && b.getVariant() == Boat.Type.BAMBOO, "bamboo keeps the raft variant");
        h.assertTrue(BoatRegistry.BOAT_UPGRADE_TEMPLATE.getDefaultInstance().get(DataComponents.RARITY) == Rarity.UNCOMMON, "template rarity");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void bigBoatSeatsThree(GameTestHelper h) {
        BigBoat boat = h.spawn(BoatRegistry.BIG_OAK_BOAT_TYPE, new Vec3(7.5, 2.1, 7.5));
        Zombie a = rider(h), b = rider(h), c = rider(h), d = rider(h);
        h.assertTrue(a.startRiding(boat) && b.startRiding(boat) && c.startRiding(boat), "three riders board");
        h.assertFalse(d.startRiding(boat), "a fourth rider is refused");
        h.startSequence().thenExecuteAfter(5, () -> {
            assertSeat(h, boat, a, 0.8, "first");
            assertSeat(h, boat, b, -0.2, "second");
            assertSeat(h, boat, c, -1.2, "third");
            boat.setYRot(90.0F);
        }).thenExecuteAfter(2, () -> {
            assertSeat(h, boat, a, 0.8, "first after turning");
            assertSeat(h, boat, c, -1.2, "third after turning");
        }).thenSucceed();
    }

    @GameTest(template = "platform")
    public static void hugeBoatSeatsFour(GameTestHelper h) {
        HugeBoat boat = h.spawn(BoatRegistry.HUGE_SPRUCE_BOAT_TYPE, new Vec3(7.5, 2.1, 7.5));
        Zombie a = rider(h), b = rider(h), c = rider(h), d = rider(h), e = rider(h);
        h.assertTrue(a.startRiding(boat) && b.startRiding(boat) && c.startRiding(boat) && d.startRiding(boat), "four riders board");
        h.assertFalse(e.startRiding(boat), "a fifth rider is refused");
        h.startSequence().thenExecuteAfter(5, () -> {
            assertSeat(h, boat, a, 1.6, "first");
            assertSeat(h, boat, b, 0.35, "second");
            assertSeat(h, boat, c, -0.9, "third");
            assertSeat(h, boat, d, -2.15, "fourth");
        }).thenSucceed();
    }

    @GameTest(template = "platform")
    public static void chestTakesASeat(GameTestHelper h) {
        BigBoat big = h.spawn(BoatRegistry.BIG_BIRCH_BOAT_TYPE, new Vec3(4.5, 2.1, 7.5));
        HugeBoat huge = h.spawn(BoatRegistry.HUGE_BIRCH_BOAT_TYPE, new Vec3(10.5, 2.1, 7.5));
        big.setHasChest(true);
        huge.setHasChest(true);
        h.assertTrue(rider(h).startRiding(big) && rider(h).startRiding(big), "big boat with a chest seats two");
        h.assertFalse(rider(h).startRiding(big), "big boat with a chest refuses a third");
        h.assertTrue(rider(h).startRiding(huge) && rider(h).startRiding(huge) && rider(h).startRiding(huge), "huge boat with a chest seats three");
        h.assertFalse(rider(h).startRiding(huge), "huge boat with a chest refuses a fourth");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void upgradeRecipesWork(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        ItemStack template = BoatRegistry.BOAT_UPGRADE_TEMPLATE.getDefaultInstance();
        for (String wood : RECIPE_WOODS) {
            Item vanilla = item(wood.equals("bamboo") ? "minecraft:bamboo_raft" : "minecraft:" + wood + "_boat");
            ItemStack planks = item("minecraft:" + wood + "_planks").getDefaultInstance();
            Item big = item("big_" + wood + "_boat");
            Item huge = item("huge_" + wood + "_boat");
            var input = new SmithingRecipeInput(template, vanilla.getDefaultInstance(), planks);
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level);
            h.assertTrue(recipe.isPresent(), "no recipe from " + vanilla);
            h.assertTrue(recipe.get().value().assemble(input, level.registryAccess()).is(big), wood + " upgrades to big");
            input = new SmithingRecipeInput(template, big.getDefaultInstance(), planks);
            recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level);
            h.assertTrue(recipe.isPresent(), "no recipe from " + big);
            h.assertTrue(recipe.get().value().assemble(input, level.registryAccess()).is(huge), wood + " upgrades to huge");
        }
        ItemStack d = Items.DIAMOND.getDefaultInstance();
        var craft = CraftingInput.of(3, 3, List.of(d, template, d, d, Items.OAK_PLANKS.getDefaultInstance(), d, d, d, d));
        var duplicate = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craft, level);
        h.assertTrue(duplicate.isPresent(), "template duplication recipe");
        ItemStack result = duplicate.get().value().assemble(craft, level.registryAccess());
        h.assertTrue(result.is(BoatRegistry.BOAT_UPGRADE_TEMPLATE) && result.getCount() == 2, "duplication gives two templates");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void smithingTableUpgrades(GameTestHelper h) {
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        SmithingMenu menu = new SmithingMenu(1, player.getInventory(), ContainerLevelAccess.create(h.getLevel(), h.absolutePos(new BlockPos(1, 2, 1))));
        h.assertTrue(menu.getSlot(0).mayPlace(BoatRegistry.BOAT_UPGRADE_TEMPLATE.getDefaultInstance()), "template fits the template slot");
        menu.getSlot(0).set(BoatRegistry.BOAT_UPGRADE_TEMPLATE.getDefaultInstance());
        h.assertTrue(menu.getSlot(1).mayPlace(Items.OAK_BOAT.getDefaultInstance()), "boat fits the base slot");
        menu.getSlot(1).set(Items.OAK_BOAT.getDefaultInstance());
        h.assertTrue(menu.getSlot(2).mayPlace(Items.OAK_PLANKS.getDefaultInstance()), "planks fit the addition slot");
        menu.getSlot(2).set(Items.OAK_PLANKS.getDefaultInstance());
        h.assertTrue(menu.getSlot(3).getItem().is(BoatRegistry.BIG_OAK_BOAT), "result slot shows the big boat");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void boatSurvivesSaveAndLoad(GameTestHelper h) {
        HugeBoat boat = h.spawn(BoatRegistry.HUGE_SPRUCE_BOAT_TYPE, new Vec3(7.5, 2.1, 7.5));
        boat.setHasChest(true);
        boat.setBanner(Items.RED_BANNER.getDefaultInstance());
        boat.setItem(3, new ItemStack(Items.DIAMOND, 5));
        boat.setYRot(45.0F);
        Zombie passenger = rider(h);
        passenger.startRiding(boat);
        h.startSequence().thenExecuteAfter(3, () -> {
            for (FakeBoat fake : fakesAround(h, boat)) {
                h.assertFalse(fake.shouldBeSaved(), "fake boats are not saved");
            }
            CompoundTag tag = new CompoundTag();
            h.assertTrue(boat.save(tag), "boat saves");
            passenger.discard();
            boat.discard();

            Entity loaded = EntityType.loadEntityRecursive(tag, h.getLevel(), e -> e);
            h.assertTrue(loaded instanceof HugeBoat, "loaded as a huge boat, got " + loaded);
            HugeBoat again = (HugeBoat) loaded;
            h.assertTrue(again.getType() == BoatRegistry.HUGE_SPRUCE_BOAT_TYPE, "wood kept");
            h.assertTrue(again.getVariant() == Boat.Type.SPRUCE, "variant kept");
            h.assertTrue(again.hasChest(), "chest kept");
            h.assertTrue(again.getBanner().is(Items.RED_BANNER), "banner kept");
            h.assertTrue(again.getItem(3).is(Items.DIAMOND) && again.getItem(3).getCount() == 5, "contents kept");
            h.assertTrue(Math.abs(again.getYRot() - 45.0F) < 0.01F, "yaw kept");
            h.assertTrue(again.getPassengers().size() == 1 && again.getFirstPassenger() instanceof Zombie, "passenger kept");
            h.getLevel().addFreshEntityWithPassengers(again);
        }).thenExecuteAfter(4, () -> {
            HugeBoat again = h.getLevel().getEntitiesOfClass(HugeBoat.class, new AABB(h.absolutePos(new BlockPos(7, 2, 7))).inflate(3)).stream()
                    .filter(Entity::isAlive).findFirst().orElse(null);
            h.assertTrue(again != null, "loaded boat is in the world");
            long owned = fakesAround(h, again).stream().filter(f -> f.owner == again).count();
            h.assertTrue(owned == 2, "loaded boat grew two fake boats, found " + owned);
            again.ejectPassengers();
            again.discard();
        }).thenSucceed();
    }

    @GameTest(template = "platform")
    public static void fakeBoatsFollowAndForward(GameTestHelper h) {
        BigBoat boat = h.spawn(BoatRegistry.BIG_ACACIA_BOAT_TYPE, new Vec3(7.5, 2.1, 7.5));
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        h.startSequence().thenExecuteAfter(3, () -> {
            List<FakeBoat> fakes = fakesAround(h, boat);
            h.assertTrue(fakes.size() == 2, "two fake boats, found " + fakes.size());
            for (FakeBoat fake : fakes) {
                h.assertTrue(fake.owner == boat, "fake boat knows its owner");
                Vec3 offset = fake.position().subtract(boat.position());
                h.assertTrue(Math.abs(Math.abs(offset.z) - 1.15) < 0.05 && Math.abs(offset.x) < 0.05, "fake boat at " + offset);
            }
            FakeBoat fake = fakes.get(0);
            h.assertTrue(fake.getPickResult().is(BoatRegistry.BIG_ACACIA_BOAT), "picking a fake boat gives the boat");
            fake.hurt(h.getLevel().damageSources().generic(), 1.0F);
            h.assertTrue(boat.getDamage() > 0.0F, "hitting a fake boat hurts the boat");
            fake.interact(player, InteractionHand.MAIN_HAND);
            h.assertTrue(boat.hasPassenger(player), "using a fake boat boards the boat");
            float damage = boat.getDamage();
            fake.hurt(h.getLevel().damageSources().playerAttack(player), 1.0F);
            h.assertTrue(boat.getDamage() <= damage, "a passenger can't hit their own boat through a fake boat");
            player.stopRiding();
            boat.discard();
            for (FakeBoat f : fakes) {
                h.assertTrue(f.isRemoved(), "fake boats go with the boat");
            }
        }).thenSucceed();
    }

    @GameTest(template = "platform")
    public static void orphanFakeBoatRemovesItself(GameTestHelper h) {
        FakeBoat fake = h.spawn(BoatRegistry.FAKE_BOAT, new Vec3(7.5, 2.1, 7.5));
        h.startSequence().thenExecuteAfter(12, () -> h.assertTrue(fake.isRemoved(), "orphan fake boat is gone")).thenSucceed();
    }

    @GameTest(template = "platform")
    public static void mobsBoardThroughTheFakeBoat(GameTestHelper h) {
        BigBoat boat = h.spawn(BoatRegistry.BIG_CHERRY_BOAT_TYPE, new Vec3(7.5, 2.1, 7.5));
        // Past the hull's own hit box at the bow, inside the fake boat's.
        Zombie zombie = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(7.5, 2.1, 9.3));
        h.succeedWhen(() -> h.assertTrue(zombie.getVehicle() == boat, "zombie boards at the bow"));
    }

    @GameTest(template = "platform")
    public static void chestBannerAndShears(GameTestHelper h) {
        BigBoat boat = h.spawn(BoatRegistry.BIG_JUNGLE_BOAT_TYPE, new Vec3(7.5, 2.1, 7.5));
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST, 2));
        boat.interact(player, InteractionHand.MAIN_HAND);
        h.assertTrue(boat.hasChest(), "chest fitted");
        h.assertTrue(player.getMainHandItem().getCount() == 1, "chest used up");
        boat.interact(player, InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().getCount() == 1, "a second chest isn't taken");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLUE_BANNER, 2));
        float before = boat.getSpeed();
        boat.interact(player, InteractionHand.MAIN_HAND);
        h.assertTrue(boat.getBanner().is(Items.BLUE_BANNER) && boat.getBanner().getCount() == 1, "banner fitted");
        h.assertTrue(player.getMainHandItem().getCount() == 1, "banner used up");
        h.assertTrue(Math.abs(boat.getSpeed() - before - 0.15F) < 0.001F, "banner adds speed");

        player.setItemInHand(InteractionHand.MAIN_HAND, Items.SHEARS.getDefaultInstance());
        boat.interact(player, InteractionHand.MAIN_HAND);
        h.assertTrue(boat.getBanner().isEmpty(), "shears take the banner off");
        h.assertTrue(player.getMainHandItem().getDamageValue() == 1, "shears wear down");
        List<ItemEntity> items = h.getLevel().getEntitiesOfClass(ItemEntity.class, boat.getBoundingBox().inflate(3.0));
        h.assertTrue(items.stream().anyMatch(i -> i.getItem().is(Items.BLUE_BANNER)), "banner drops");
        items.forEach(Entity::discard);
        h.assertFalse(boat.canBeLeashed(), "big boats can't be leashed");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void brokenBoatDropsItsParts(GameTestHelper h) {
        BigBoat boat = h.spawn(BoatRegistry.BIG_DARK_OAK_BOAT_TYPE, new Vec3(7.5, 2.1, 7.5));
        boat.setHasChest(true);
        boat.setBanner(Items.GREEN_BANNER.getDefaultInstance());
        boat.setItem(0, new ItemStack(Items.DIAMOND, 3));
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        boat.hurt(h.getLevel().damageSources().playerAttack(player), 100.0F);
        h.assertTrue(boat.isRemoved(), "boat broke");
        List<ItemEntity> items = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(new BlockPos(7, 2, 7))).inflate(4));
        int boats = 0, banners = 0, diamonds = 0, chests = 0;
        for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();
            if (stack.is(BoatRegistry.BIG_DARK_OAK_BOAT)) boats += stack.getCount();
            if (stack.is(Items.GREEN_BANNER)) banners += stack.getCount();
            if (stack.is(Items.DIAMOND)) diamonds += stack.getCount();
            if (stack.is(Items.CHEST)) chests += stack.getCount();
        }
        NekomasFixed.LOGGER.info("Broken big boat dropped {} boats, {} chests, {} banners, {} diamonds", boats, chests, banners, diamonds);
        h.assertTrue(boats == 1, "boat item drops once, got " + boats);
        h.assertTrue(banners == 1, "banner drops once, got " + banners);
        h.assertTrue(diamonds == 3, "contents drop, got " + diamonds);
        h.assertTrue(chests >= 1, "chest drops");
        items.forEach(Entity::discard);
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void hopperOnlyReachesAFittedChest(GameTestHelper h) {
        BlockPos hopperAt = new BlockPos(7, 2, 7);
        h.setBlock(hopperAt, Blocks.HOPPER);
        BigBoat boat = h.spawn(BoatRegistry.BIG_OAK_BOAT_TYPE, new Vec3(7.5, 3.1, 7.5));
        boat.setItem(0, new ItemStack(Items.DIAMOND));
        HopperBlockEntity hopper = (HopperBlockEntity) h.getBlockEntity(hopperAt);
        h.startSequence().thenExecuteAfter(40, () -> {
            h.assertTrue(hopper.getItem(0).isEmpty(), "hopper took from a boat with no chest");
            h.assertTrue(boat.getItem(0).is(Items.DIAMOND), "the item stays in the boat");
            boat.setHasChest(true);
        }).thenWaitUntil(() -> h.assertTrue(hopper.getItem(0).is(Items.DIAMOND), "hopper takes from a boat with a chest")).thenSucceed();
    }

    @GameTest(template = "platform")
    public static void placingFromTheItem(GameTestHelper h) {
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        Vec3 at = h.absoluteVec(new Vec3(7.5, 3.5, 7.5));
        player.moveTo(at.x, at.y, at.z, 30.0F, 90.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BoatRegistry.HUGE_BAOBAB_BOAT));
        BoatRegistry.HUGE_BAOBAB_BOAT.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().isEmpty(), "boat item is used up");
        List<HugeBoat> boats = h.getLevel().getEntitiesOfClass(HugeBoat.class, new AABB(at, at).inflate(4));
        h.assertTrue(boats.size() == 1 && boats.get(0).getType() == BoatRegistry.HUGE_BAOBAB_BOAT_TYPE, "boat placed, found " + boats);
        h.assertTrue(Math.abs(boats.get(0).getYRot() - 30.0F) < 0.01F, "boat faces the player's way");
        h.succeed();
    }

    private static void steers(GameTestHelper h, Boat boat) {
        fillPool(h);
        Pillager pillager = h.spawnWithNoFreeWill(EntityType.PILLAGER, new Vec3(7.5, 3.5, 7.5));
        h.assertTrue(pillager.startRiding(boat), "pillager boards");
        Zombie target = EntityType.ZOMBIE.create(h.getLevel());
        Vec3 away = h.absoluteVec(new Vec3(30.5, 3, 7.5));
        target.setPos(away.x, away.y, away.z);
        pillager.setTarget(target);
        double startX = boat.getX();
        h.succeedWhen(() -> {
            h.assertTrue(boat.getYRot() < -20.0F, "boat turns toward the target, yaw " + boat.getYRot());
            h.assertTrue(boat.getX() > startX + 0.3, "boat moves toward the target, moved " + (boat.getX() - startX));
        });
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void pillagerSteersABigBoat(GameTestHelper h) {
        steers(h, h.spawn(BoatRegistry.BIG_OAK_BOAT_TYPE, new Vec3(7.5, 3.0, 7.5)));
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void pillagerSteersAPlainBoat(GameTestHelper h) {
        steers(h, h.spawn(EntityType.BOAT, new Vec3(7.5, 3.0, 7.5)));
    }

    @GameTest(template = "platform")
    public static void boatSpeedsMatch(GameTestHelper h) {
        BigBoat big = h.spawn(BoatRegistry.BIG_OAK_BOAT_TYPE, new Vec3(4.5, 2.1, 7.5));
        HugeBoat huge = h.spawn(BoatRegistry.HUGE_OAK_BOAT_TYPE, new Vec3(10.5, 2.1, 7.5));
        h.assertTrue(Math.abs(big.getSpeed() - 0.4F) < 0.001F && Math.abs(huge.getSpeed() - 0.3F) < 0.001F, "empty speeds");
        h.assertTrue(big.getRotationSpeed() == 0.6F && huge.getRotationSpeed() == 0.4F, "rotation speeds");
        huge.setBanner(Items.WHITE_BANNER.getDefaultInstance());
        for (int i = 0; i < 4; i++) {
            h.spawnWithNoFreeWill(EntityType.PILLAGER, new Vec3(1.5, 2, 1.5)).startRiding(huge);
        }
        h.assertTrue(huge.countRowable() == 4, "pillagers row");
        h.assertTrue(Math.abs(huge.getSpeed() - 0.6F) < 0.001F, "raiders are capped at 0.6, got " + huge.getSpeed());
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void pillagersShootFurther(GameTestHelper h) throws ReflectiveOperationException {
        Pillager pillager = h.spawn(EntityType.PILLAGER, new Vec3(7.5, 2, 7.5));
        boolean crossbow = false, hold = false;
        for (var wrapped : pillager.goalSelector.getAvailableGoals()) {
            var goal = wrapped.getGoal();
            if (goal instanceof RangedCrossbowAttackGoal<?>) {
                Field field = RangedCrossbowAttackGoal.class.getDeclaredField("attackRadiusSqr");
                field.setAccessible(true);
                h.assertTrue(field.getFloat(goal) == 144.0F, "crossbow range is 12 blocks, squared " + field.getFloat(goal));
                crossbow = true;
            } else if (goal.getClass().getSimpleName().equals("HoldGroundAttackGoal")) {
                Field field = goal.getClass().getDeclaredField("hostileRadiusSqr");
                field.setAccessible(true);
                h.assertTrue(field.getFloat(goal) == 225.0F, "hold ground radius is 15 blocks, squared " + field.getFloat(goal));
                hold = true;
            }
        }
        h.assertTrue(crossbow && hold, "pillager has both goals");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void shipwrecksHoldTheTemplate(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SHIPWRECK_TREASURE);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(h.absolutePos(new BlockPos(1, 2, 1))))
                .create(LootContextParamSets.CHEST);
        boolean found = false;
        for (int i = 0; i < 40 && !found; i++) {
            found = table.getRandomItems(params).stream().anyMatch(s -> s.is(BoatRegistry.BOAT_UPGRADE_TEMPLATE));
        }
        h.assertTrue(found, "no template in 40 shipwreck treasure rolls");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void shipOfThesusLoads(GameTestHelper h) {
        var advancements = h.getLevel().getServer().getAdvancements();
        var ship = advancements.get(NekomasFixed.id("mod/ship_of_thesus"));
        h.assertTrue(ship != null, "advancement missing");
        h.assertTrue(ship.value().parent().isPresent() && advancements.get(ship.value().parent().get()) != null, "parent advancement missing");
        h.assertTrue(ship.value().criteria().size() == 10, "one criterion per wood, got " + ship.value().criteria().size());
        h.succeed();
    }

    /** Drives the real spawner over a far-off patch of ocean, in a batch of its own because it changes the time of day. */
    @GameTest(template = "platform", batch = "boat_patrol", timeoutTicks = 200)
    public static void patrolsSailOnTheOcean(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        // A new patch each run: a kept test world may still hold the ships of an earlier one.
        BlockPos centre = new BlockPos(20000 + 512 * level.random.nextInt(4000), -56, 20000);
        int chunkX = centre.getX() >> 4, chunkZ = centre.getZ() >> 4;
        for (int cx = chunkX - 4; cx <= chunkX + 4; cx++) {
            for (int cz = chunkZ - 4; cz <= chunkZ + 4; cz++) {
                level.setChunkForced(cx, cz, true);
                level.getChunk(cx, cz);
                int x = cx << 4, z = cz << 4;
                level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),
                        "fillbiome " + x + " -64 " + z + " " + (x + 15) + " -49 " + (z + 15) + " minecraft:ocean");
            }
        }
        BlockPos.MutableBlockPos water = new BlockPos.MutableBlockPos();
        for (int x = -63; x <= 63; x++) {
            for (int z = -63; z <= 63; z++) {
                for (int y = -60; y <= -57; y++) {
                    level.setBlock(water.set(centre.getX() + x, y, centre.getZ() + z), Blocks.WATER.defaultBlockState(), 2 | 16);
                }
            }
        }
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        player.moveTo(centre.getX() + 0.5, centre.getY(), centre.getZ() + 0.5);
        long oldTime = level.getDayTime();
        level.setDayTime(24000L * 6 + 1000L);
        h.startSequence().thenExecuteAfter(2, () -> {
            h.assertTrue(level.getBiome(centre).is(net.minecraft.tags.BiomeTags.IS_OCEAN), "patch should be ocean");
            h.assertTrue(level.isDay(), "should be day");
            PatrolSpawner spawner = new PatrolSpawner();
            int spawned = 0;
            for (int i = 0; i < 4_000_000 && spawned == 0; i++) {
                spawned = spawner.tick(level, true, false);
            }
            h.assertTrue(spawned > 0, "no patrol spawned");
            AABB area = new AABB(centre).inflate(80);
            List<BigBoat> captains = level.getEntitiesOfClass(BigBoat.class, area);
            h.assertTrue(captains.size() == 1, "one captain's ship, found " + captains.size());
            BigBoat captain = captains.get(0);
            h.assertTrue(captain.hasChest(), "captain's ship has a chest");
            h.assertTrue(captain.getBanner().is(Items.WHITE_BANNER) && !captain.getBanner().getComponentsPatch().isEmpty(), "captain's ship flies the ominous banner");
            h.assertTrue(captain.getPassengers().size() == level.getDifficulty().getId() && captain.getFirstPassenger() instanceof Pillager, "pillagers per difficulty, found " + captain.getPassengers());
            ResourceKey<LootTable> chests = ResourceKey.create(Registries.LOOT_TABLE, NekomasFixed.id("chests/patrol_boat"));
            h.assertTrue(chests.equals(captain.getLootTable()), "captain's chest loot table");
            captain.unpackLootTable(null);
            boolean template = false, bottle = false;
            for (int i = 0; i < captain.getContainerSize(); i++) {
                template |= captain.getItem(i).is(BoatRegistry.BOAT_UPGRADE_TEMPLATE);
                bottle |= captain.getItem(i).is(Items.OMINOUS_BOTTLE);
            }
            h.assertTrue(template && bottle, "captain's chest holds the template and an ominous bottle");
            List<Boat> escorts = level.getEntitiesOfClass(Boat.class, area, b -> !(b instanceof BigBoat));
            h.assertTrue(spawned == 1 + escorts.size(), "escort boats, spawned " + spawned + ", found " + escorts.size());
            h.assertTrue(!escorts.isEmpty() && escorts.stream().allMatch(b -> b.getFirstPassenger() instanceof Pillager), "each escort has a pillager");
            NekomasFixed.LOGGER.info("Patrol at sea: {} ships led by {}, {} pillagers aboard", spawned, captain.getType().getDescriptionId(), captain.getPassengers().size());

            for (Entity entity : level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
                entity.discard();
            }
            level.setDayTime(oldTime);
            player.discard();
            for (int cx = chunkX - 4; cx <= chunkX + 4; cx++) {
                for (int cz = chunkZ - 4; cz <= chunkZ + 4; cz++) {
                    level.setChunkForced(cx, cz, false);
                }
            }
        }).thenSucceed();
    }
}
