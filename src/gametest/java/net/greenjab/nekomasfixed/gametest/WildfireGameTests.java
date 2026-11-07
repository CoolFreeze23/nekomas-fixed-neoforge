package net.greenjab.nekomasfixed.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.entity.WildFire.FireBomb;
import net.greenjab.nekomasfixed.registry.entity.WildFire.WildfireEntity;
import net.greenjab.nekomasfixed.registry.entity.WildfireTrident;
import net.greenjab.nekomasfixed.registry.registries.WildfireRegistry;
import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.function.Predicate;

@GameTestHolder(NekomasFixed.NAMESPACE)
@PrefixGameTestTemplate(false)
public class WildfireGameTests {
    private static final String PLAYER_BATCH = "wildfire_with_player";

    /** The helper's mock server player counts as creative, which trial spawners and shields shouldn't see. */
    private static ServerPlayer survivalPlayer(GameTestHelper h, Vec3 pos) {
        ServerLevel level = h.getLevel();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "wildfire-test-player"), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(h.absoluteVec(pos));
        return player;
    }

    private static WildfireEntity spawnWildfire(GameTestHelper h, double x, double y, double z) {
        return h.spawn(WildfireRegistry.WILDFIRE, new Vec3(x, y, z));
    }

    @GameTest(template = "platform")
    public static void wildfireHasBossStatsAndSaves(GameTestHelper h) {
        WildfireEntity wildfire = spawnWildfire(h, 1.5, 2, 1.5);
        h.assertTrue(wildfire.getMaxHealth() == 150.0F, "max health " + wildfire.getMaxHealth());
        h.assertTrue(wildfire.fireImmune(), "fire immune");
        h.assertTrue(wildfire.getShieldsActive() == 4, "starts with four shields");
        h.assertTrue(wildfire.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE), "fall damage tag");
        h.assertTrue(wildfire.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES), "freeze tag");

        wildfire.setSoulActive(true);
        wildfire.setShieldsActive(2);
        CompoundTag saved = wildfire.saveWithoutId(new CompoundTag());
        WildfireEntity copy = WildfireRegistry.WILDFIRE.create(h.getLevel());
        copy.load(saved);
        h.assertTrue(copy.isSoulActive(), "soul flag saved");
        h.assertTrue(copy.getShieldsActive() == 2, "shield count saved");
        copy.discard();
        wildfire.discard();
        h.succeed();
    }

    private static Cow toughCow(GameTestHelper h, double x, double z) {
        Cow cow = h.spawn(EntityType.COW, new Vec3(x, 2, z));
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        cow.setHealth(1000);
        cow.setNoAi(true);
        return cow;
    }

    /** The whole loop on its own: it finds the cow, picks an attack by itself and uses it. */
    @GameTest(template = "platform", batch = "wildfire_ai", timeoutTicks = 1200)
    public static void wildfireTargetsAndAttacks(GameTestHelper h) {
        Cow cow = toughCow(h, 1.5, 3.5);
        WildfireEntity wildfire = spawnWildfire(h, 1.5, 2, 1.5);
        boolean[] targeted = new boolean[1];
        h.onEachTick(() -> {
            if (wildfire.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(target -> target == cow).isPresent()) targeted[0] = true;
            String attack = attackSeen(h, wildfire, cow);
            if (targeted[0] && attack != null) {
                NekomasFixed.LOGGER.info("wildfireTargetsAndAttacks: {} after {} ticks", attack, h.getTick());
                wildfire.discard();
                cow.discard();
                h.succeed();
            }
        });
    }

    private static String attackSeen(GameTestHelper h, WildfireEntity wildfire, Cow cow) {
        if (!h.getLevel().getEntitiesOfClass(SmallFireball.class, wildfire.getBoundingBox().inflate(32), ball -> ball.getOwner() == wildfire).isEmpty()) return "fireball";
        if (!h.getLevel().getEntitiesOfClass(FireBomb.class, wildfire.getBoundingBox().inflate(32), bomb -> bomb.getOwner() == wildfire).isEmpty()) return "bomb";
        if (cow.getHealth() < 1000) return "damage";
        return null;
    }

    /** Waits for the target, then starts the attack whose pose the task looks for, and waits for its effect. */
    private static void forceAttack(GameTestHelper h, Pose pose, java.util.function.BiPredicate<WildfireEntity, Cow> done) {
        Cow cow = toughCow(h, 1.5, 3.5);
        WildfireEntity wildfire = spawnWildfire(h, 1.5, 2, 1.5);
        boolean[] started = new boolean[1];
        h.onEachTick(() -> {
            if (!started[0] && wildfire.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                // a hit ends the slide task, which then picks a random attack pose: swap it for the one under test
                wildfire.hurtTime = 10;
                if (wildfire.getPose() != Pose.STANDING) {
                    wildfire.setPose(pose);
                    started[0] = true;
                }
            }
            if (started[0] && done.test(wildfire, cow)) {
                NekomasFixed.LOGGER.info("{} attack worked after {} ticks", pose, h.getTick());
                wildfire.discard();
                cow.discard();
                h.succeed();
            }
        });
    }

    @GameTest(template = "platform", batch = "wildfire_shoot", timeoutTicks = 400)
    public static void wildfireShootsFireballs(GameTestHelper h) {
        forceAttack(h, Pose.SHOOTING, (wildfire, cow) -> wildfire.isOnFire()
                && !h.getLevel().getEntitiesOfClass(SmallFireball.class, wildfire.getBoundingBox().inflate(32), ball -> ball.getOwner() == wildfire).isEmpty());
    }

    @GameTest(template = "platform", batch = "wildfire_melee", timeoutTicks = 400)
    public static void wildfireMeleeBurnsNearby(GameTestHelper h) {
        forceAttack(h, Pose.SPIN_ATTACK, (wildfire, cow) -> wildfire.isOnFire() && cow.getHealth() < 1000);
    }

    @GameTest(template = "platform", batch = "wildfire_jump", timeoutTicks = 600)
    public static void wildfireJumpsAndDropsBombs(GameTestHelper h) {
        forceAttack(h, Pose.LONG_JUMPING, (wildfire, cow) ->
                !h.getLevel().getEntitiesOfClass(FireBomb.class, wildfire.getBoundingBox().inflate(32), bomb -> bomb.getOwner() == wildfire).isEmpty());
    }

    @GameTest(template = "platform")
    public static void wildfireComesBackAsSoulPhase(GameTestHelper h) {
        WildfireEntity wildfire = spawnWildfire(h, 1.5, 2, 1.5);
        wildfire.setNoAi(true);
        DamageSource generic = h.getLevel().damageSources().generic();
        wildfire.hurt(generic, 1000);
        h.assertTrue(wildfire.isAlive(), "first death is survived");
        h.assertTrue(wildfire.isSoulActive(), "soul phase active");
        h.assertTrue(wildfire.getHealth() == wildfire.getMaxHealth(), "health restored, was " + wildfire.getHealth());
        wildfire.invulnerableTime = 0;
        wildfire.hurt(generic, 1000);
        h.assertTrue(wildfire.isDeadOrDying(), "second death is final");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void shieldsStopArrowsUnlessBurning(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        WildfireEntity wildfire = spawnWildfire(h, 1.5, 2, 1.5);
        wildfire.setNoAi(true);
        Arrow arrow = new Arrow(EntityType.ARROW, level);
        h.assertFalse(wildfire.hurt(level.damageSources().arrow(arrow, null), 5), "four shields always stop an arrow");
        wildfire.setFireActive(true);
        h.assertTrue(wildfire.hurt(level.damageSources().arrow(arrow, null), 5), "open shields let it through");
        h.assertTrue(wildfire.getHealth() == 145.0F, "health " + wildfire.getHealth());
        wildfire.discard();
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 100)
    public static void snowballsHurtTheWildfire(GameTestHelper h) {
        WildfireEntity wildfire = spawnWildfire(h, 1.5, 2, 1.5);
        wildfire.setNoAi(true);
        wildfire.setNoGravity(true);
        Snowball snowball = EntityType.SNOWBALL.create(h.getLevel());
        snowball.moveTo(h.absoluteVec(new Vec3(1.5, 5.5, 1.5)));
        snowball.setDeltaMovement(0, -1, 0);
        h.getLevel().addFreshEntity(snowball);
        h.succeedWhen(() -> h.assertTrue(wildfire.getHealth() == 147.0F, "health " + wildfire.getHealth()));
    }

    @GameTest(template = "platform", timeoutTicks = 100)
    public static void fireBombExplodes(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        WildfireEntity wildfire = spawnWildfire(h, 1.5, 2, 1.5);
        wildfire.setNoAi(true);
        FireBomb bomb = new FireBomb(level, wildfire);
        bomb.setPos(h.absoluteVec(new Vec3(3.5, 4.5, 3.5)));
        level.addFreshEntity(bomb);
        h.succeedWhen(() -> h.assertFalse(bomb.isAlive(), "bomb still flying"));
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void thrownTridentIgnitesAndDamages(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        Zombie zombie = h.spawn(EntityType.ZOMBIE, new Vec3(1.5, 2, 1.5));
        zombie.setNoAi(true);
        WildfireTrident trident = new WildfireTrident(level, h.absoluteVec(new Vec3(1.5, 5, 1.5)).x, h.absoluteVec(new Vec3(1.5, 5, 1.5)).y, h.absoluteVec(new Vec3(1.5, 5, 1.5)).z, new ItemStack(WildfireRegistry.WILDFIRE_TRIDENT));
        trident.setDeltaMovement(0, -1.5, 0);
        level.addFreshEntity(trident);
        h.succeedWhen(() -> {
            h.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "zombie not hit");
            h.assertTrue(zombie.isOnFire(), "zombie not burning");
        });
    }

    @GameTest(template = "platform")
    public static void tridentSavesAndThrows(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = new ItemStack(WildfireRegistry.WILDFIRE_TRIDENT);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.moveTo(h.absoluteVec(new Vec3(1.5, 2, 1.5)));
        player.startUsingItem(InteractionHand.MAIN_HAND);
        stack.getItem().releaseUsing(stack, level, player, 72000 - 20);
        var thrown = level.getEntitiesOfClass(WildfireTrident.class, player.getBoundingBox().inflate(4));
        h.assertTrue(thrown.size() == 1, "thrown tridents: " + thrown.size());
        WildfireTrident trident = thrown.get(0);
        h.assertTrue(trident.getPickupItemStackOrigin().is(WildfireRegistry.WILDFIRE_TRIDENT), "carries its item");
        h.assertTrue(trident.getDeltaMovement().length() > 1.0, "flies at speed");
        CompoundTag saved = trident.saveWithoutId(new CompoundTag());
        WildfireTrident copy = WildfireRegistry.WILDFIRE_TRIDENT_ENTITY.create(level);
        copy.load(saved);
        h.assertTrue(copy.getPickupItemStackOrigin().is(WildfireRegistry.WILDFIRE_TRIDENT), "item saved");
        copy.discard();
        trident.discard();
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void itemsAndRecipesLoad(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        ItemStack heart = new ItemStack(WildfireRegistry.NETHER_HEART);
        h.assertTrue(heart.get(DataComponents.RARITY) == Rarity.UNCOMMON, "heart rarity");
        h.assertTrue(heart.has(DataComponents.FIRE_RESISTANT), "heart resists fire");
        h.assertFalse(heart.canBeHurtBy(level.damageSources().explosion(null, null)), "heart survives explosions");
        ItemStack shield = new ItemStack(WildfireRegistry.WILDFIRE_SHIELD);
        h.assertTrue(shield.getMaxDamage() == 336, "shield durability");
        h.assertTrue(shield.has(DataComponents.FIRE_RESISTANT), "shield resists fire");
        h.assertTrue(shield.getItem().isValidRepairItem(shield, new ItemStack(Items.NETHERITE_INGOT)), "shield repairs with netherite");
        h.assertTrue(new ItemStack(WildfireRegistry.WILDFIRE_TRIDENT).getMaxDamage() == 1000, "trident durability");
        h.assertTrue(new ItemStack(WildfireRegistry.WILDFIRE_TRIDENT).is(net.minecraft.tags.ItemTags.TRIDENT_ENCHANTABLE), "trident enchantable");

        checkSmithing(h, "wildfire_trident", Items.TRIDENT, WildfireRegistry.WILDFIRE_TRIDENT.getDefaultInstance());
        checkSmithing(h, "wildfire_shield", Items.SHIELD, WildfireRegistry.WILDFIRE_SHIELD.getDefaultInstance());

        var tables = level.getServer().reloadableRegistries();
        for (String name : new String[]{"entities/wildfire", "spawners/wildfire", "spawners/wildfire_ominous"}) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, NekomasFixed.id(name));
            h.assertTrue(tables.getKeys(Registries.LOOT_TABLE).contains(key.location()), "loot table missing: " + name);
        }
        h.assertTrue(tables.getLootTable(WildfireRegistry.WILDFIRE.getDefaultLootTable()) != LootTable.EMPTY, "wildfire drops");
        h.succeed();
    }

    private static void checkSmithing(GameTestHelper h, String id, net.minecraft.world.item.Item base, ItemStack result) {
        ServerLevel level = h.getLevel();
        RecipeHolder<?> holder = level.getRecipeManager().byKey(NekomasFixed.id(id)).orElse(null);
        h.assertTrue(holder != null, "recipe missing: " + id);
        h.assertTrue(holder.value() instanceof SmithingTransformRecipe, id + " is a smithing transform");
        SmithingTransformRecipe recipe = (SmithingTransformRecipe) holder.value();
        SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(WildfireRegistry.NETHER_HEART), new ItemStack(base), new ItemStack(Items.NETHERITE_INGOT));
        h.assertTrue(recipe.matches(input, level), id + " takes heart, base and netherite");
        h.assertTrue(ItemStack.isSameItem(recipe.assemble(input, level.registryAccess()), result), id + " result");
    }

    @GameTest(template = "platform")
    public static void fortressRoomBuildsWithItsSpawner(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        BlockPos origin = h.absolutePos(new BlockPos(8, 100, 8));
        StructurePiecesBuilder pieces = new StructurePiecesBuilder();
        NetherFortressPieces.CastleEntrance room = NetherFortressPieces.CastleEntrance.createPiece(
                pieces, level.getRandom(), origin.getX(), origin.getY(), origin.getZ(), Direction.NORTH, 0);
        h.assertTrue(room != null, "piece refused");
        BoundingBox box = room.getBoundingBox();
        h.assertTrue(box.getXSpan() == 17 && box.getYSpan() == 15 && box.getZSpan() == 17, "room size " + box);
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        room.postProcess(level, level.structureManager(), generator, level.getRandom(), box, new ChunkPos(origin), origin);

        BlockPos spawnerPos = null;
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            if (level.getBlockState(pos).is(Blocks.TRIAL_SPAWNER)) spawnerPos = pos.immutable();
        }
        h.assertTrue(spawnerPos != null, "no trial spawner in the room");
        h.assertTrue(level.getBlockEntity(spawnerPos) instanceof TrialSpawnerBlockEntity, "spawner block entity");
        TrialSpawnerBlockEntity spawner = (TrialSpawnerBlockEntity) level.getBlockEntity(spawnerPos);
        checkConfig(h, spawner.getTrialSpawner().getNormalConfig(), 1.0F, "spawners/wildfire");
        checkConfig(h, spawner.getTrialSpawner().getOminousConfig(), 2.0F, "spawners/wildfire_ominous");
        h.assertTrue(level.getBlockState(spawnerPos.above()).is(Blocks.LAVA), "lava pool above the spawner");
        int fires = 0;
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            if (level.getBlockState(pos).is(BlockTags.FIRE)) fires++;
        }
        h.assertTrue(fires == 4, "four fires in the corners, found " + fires);

        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16);
        }
        h.succeed();
    }

    private static void checkConfig(GameTestHelper h, TrialSpawnerConfig config, float mobs, String lootTable) {
        h.assertTrue(config.totalMobs() == mobs && config.simultaneousMobs() == mobs, "mob counts for " + lootTable);
        h.assertTrue(config.totalMobsAddedPerPlayer() == 0.0F, "no extra mobs per player");
        h.assertTrue(config.spawnRange() == 1 && config.ticksBetweenSpawn() == 20, "spawn timing for " + lootTable);
        h.assertTrue(config.spawnPotentialsDefinition().unwrap().size() == 1, "one spawn potential");
        h.assertTrue(config.spawnPotentialsDefinition().unwrap().get(0).data().getEntityToSpawn().getString("id").equals("nekomasfixed:wildfire"), "spawns the wildfire");
        h.assertTrue(config.lootTablesToEject().unwrap().get(0).data().location().equals(NekomasFixed.id(lootTable)), "loot table " + lootTable);
    }

    /** Real fortress starts: the piece mixin has to work inside the normal structure assembly too. */
    @GameTest(template = "platform", timeoutTicks = 200)
    public static void realFortressesContainTheBossRoom(GameTestHelper h) {
        ServerLevel nether = h.getLevel().getServer().getLevel(Level.NETHER);
        h.assertTrue(nether != null, "the test server has no nether");
        Structure fortress = nether.registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.FORTRESS);
        h.assertTrue(fortress != null, "fortress structure");
        ChunkGenerator generator = nether.getChunkSource().getGenerator();
        Predicate<Holder<Biome>> anyBiome = biome -> true;
        int rooms = 0;
        for (int seed = 0; seed < 40 && rooms == 0; seed++) {
            StructureStart start = fortress.generate(nether.registryAccess(), generator, generator.getBiomeSource(),
                    nether.getChunkSource().randomState(), nether.getStructureManager(), seed, new ChunkPos(seed * 7, seed * 3),
                    0, nether, anyBiome);
            if (!start.isValid()) continue;
            for (StructurePiece piece : start.getPieces()) {
                if (piece instanceof NetherFortressPieces.CastleEntrance) {
                    BoundingBox box = piece.getBoundingBox();
                    h.assertTrue(box.getYSpan() == 15, "room height " + box);
                    h.assertTrue(Math.max(box.getXSpan(), box.getZSpan()) == 17 && Math.min(box.getXSpan(), box.getZSpan()) == 17, "room footprint " + box);
                    rooms++;
                }
            }
        }
        h.assertTrue(rooms > 0, "no boss room in 40 fortresses");
        h.succeed();
    }

    @GameTest(template = "platform", batch = PLAYER_BATCH, timeoutTicks = 200)
    public static void trialSpawnerBringsTheWildfire(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        BlockPos pos = new BlockPos(1, 2, 1);
        h.setBlock(pos, Blocks.TRIAL_SPAWNER);
        TrialSpawnerBlockEntity spawner = (TrialSpawnerBlockEntity) level.getBlockEntity(h.absolutePos(pos));

        CompoundTag entity = new CompoundTag();
        entity.putString("id", "nekomasfixed:wildfire");
        CompoundTag spawnData = new CompoundTag();
        spawnData.put("entity", entity);
        CompoundTag potential = new CompoundTag();
        potential.put("data", spawnData);
        potential.putInt("weight", 1);
        net.minecraft.nbt.ListTag potentials = new net.minecraft.nbt.ListTag();
        potentials.add(potential);
        CompoundTag config = new CompoundTag();
        config.put("spawn_potentials", potentials);
        config.putFloat("total_mobs", 1.0F);
        config.putFloat("simultaneous_mobs", 1.0F);
        config.putInt("ticks_between_spawn", 20);
        config.putInt("spawn_range", 1);
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "minecraft:trial_spawner");
        nbt.put("normal_config", config);
        spawner.loadWithComponents(nbt, level.registryAccess());
        spawner.getTrialSpawner().overridePeacefulAndMobSpawnRule();

        ServerPlayer player = survivalPlayer(h, new Vec3(4.5, 2, 1.5));
        h.succeedWhen(() -> {
            h.assertEntityPresent(WildfireRegistry.WILDFIRE);
            level.getServer().getPlayerList().remove(player);
            h.getEntities(WildfireRegistry.WILDFIRE).forEach(WildfireEntity::discard);
        });
    }

    @GameTest(template = "platform", batch = PLAYER_BATCH)
    public static void wildfireShieldBurnsTheAttacker(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        ServerPlayer player = survivalPlayer(h, new Vec3(1.5, 2, 1.5));
        player.setYRot(0);
        for (int i = 0; i < 61; i++) player.tick(); // the join protection runs out
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(WildfireRegistry.WILDFIRE_SHIELD));
        player.startUsingItem(InteractionHand.OFF_HAND);
        for (int i = 0; i < 6; i++) player.doTick();
        Zombie zombie = h.spawn(EntityType.ZOMBIE, new Vec3(1.5, 2, 3.5));
        zombie.setNoAi(true);
        h.assertTrue(player.isBlocking(), "player raised the shield");
        float health = player.getHealth();
        player.hurt(level.damageSources().mobAttack(zombie), 5);
        h.assertTrue(player.getHealth() == health, "the blow was blocked");
        h.assertTrue(zombie.getRemainingFireTicks() > 0, "attacker burns");
        level.getServer().getPlayerList().remove(player);
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void spawnOptionBlocksTheWildfire(GameTestHelper h) {
        boolean before = ModConfigValues.spawnWildfire.get();
        ModConfigValues.spawnWildfire.set(false);
        try {
            WildfireEntity wildfire = WildfireRegistry.WILDFIRE.create(h.getLevel());
            wildfire.moveTo(h.absoluteVec(new Vec3(1.5, 2, 1.5)));
            h.assertFalse(h.getLevel().addFreshEntity(wildfire), "disabled entity still spawned");
        } finally {
            ModConfigValues.spawnWildfire.set(before);
        }
        h.succeed();
    }
}
