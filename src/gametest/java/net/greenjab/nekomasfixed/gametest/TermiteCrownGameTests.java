package net.greenjab.nekomasfixed.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.GoatHornBlock;
import net.greenjab.nekomasfixed.registry.block.TermitehiveBlock;
import net.greenjab.nekomasfixed.registry.block.entity.GoatHornBlockEntity;
import net.greenjab.nekomasfixed.registry.block.entity.TermitehiveBlockEntity;
import net.greenjab.nekomasfixed.registry.block.enums.GoatHornTorchType;
import net.greenjab.nekomasfixed.registry.entity.Termite;
import net.greenjab.nekomasfixed.registry.item.CrownItem;
import net.greenjab.nekomasfixed.registry.item.FlowerCrownItem;
import net.greenjab.nekomasfixed.registry.item.GoatHornHelmetItem;
import net.greenjab.nekomasfixed.registry.other.TermitesComponent;
import net.greenjab.nekomasfixed.registry.registries.BlockRegistry;
import net.greenjab.nekomasfixed.registry.registries.CrownRegistry;
import net.greenjab.nekomasfixed.registry.registries.GoatHornRegistry;
import net.greenjab.nekomasfixed.registry.registries.TermiteRegistry;
import net.greenjab.nekomasfixed.registry.registries.TrimRegistry;
import net.greenjab.nekomasfixed.registry.worldgen.ModFeatures;
import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.greenjab.nekomasfixed.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.Reader;
import java.util.List;

@GameTestHolder(NekomasFixed.NAMESPACE)
@PrefixGameTestTemplate(false)
public class TermiteCrownGameTests {
    private static final BlockPos AT = new BlockPos(7, 2, 7);

    private static TermitehiveBlockEntity hive(GameTestHelper h, BlockPos pos) {
        h.setBlock(pos, TermiteRegistry.TERMITE_HIVE);
        return (TermitehiveBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(pos));
    }

    private static TermitehiveBlockEntity.TermiteData parked() {
        return new TermitehiveBlockEntity.TermiteData(TermitehiveBlockEntity.TermiteData.create(0).entityData(), 0, 100000);
    }

    @GameTest(template = "platform")
    public static void hiveCountFollowsTheBlockState(GameTestHelper h) {
        TermitehiveBlockEntity be = hive(h, AT);
        be.addTermite(parked());
        h.runAfterDelay(2, () -> {
            h.assertBlockProperty(AT, TermitehiveBlock.TERMITES, 1);
            be.addTermite(parked());
            h.assertTrue(be.isFullOfTermites(), "two termites fill a hive");
            h.runAfterDelay(2, () -> {
                h.assertBlockProperty(AT, TermitehiveBlock.TERMITES, 2);
                h.succeed();
            });
        });
    }

    @GameTest(template = "platform", timeoutTicks = 100)
    public static void hiveReleasesTermitesWhenTheirTimeIsUp(GameTestHelper h) {
        TermitehiveBlockEntity be = hive(h, AT);
        be.addTermite(TermitehiveBlockEntity.TermiteData.create(598));
        h.succeedWhen(() -> {
            h.assertTrue(be.hasNoTermites(), "termite should have left");
            h.assertEntityPresent(TermiteRegistry.TERMITE);
            h.assertBlockProperty(AT, TermitehiveBlock.TERMITES, 0);
        });
    }

    @GameTest(template = "platform")
    public static void hiveSavesAndCopiesToItems(GameTestHelper h) {
        TermitehiveBlockEntity be = hive(h, AT);
        be.addTermite(parked());
        be.addTermite(parked());
        var registries = h.getLevel().registryAccess();
        CompoundTag saved = be.saveWithoutMetadata(registries);
        h.assertTrue(saved.contains("termites"), "saved tag has no termites: " + saved);

        TermitehiveBlockEntity loaded = new TermitehiveBlockEntity(h.absolutePos(AT), be.getBlockState());
        loaded.loadWithComponents(saved, registries);
        h.assertTrue(loaded.getTermiteCount() == 2, "loaded " + loaded.getTermiteCount());

        ItemStack stack = new ItemStack(TermiteRegistry.TERMITE_HIVE_ITEM);
        stack.applyComponents(be.collectComponents());
        TermitesComponent component = stack.get(TermiteRegistry.TERMITES);
        h.assertTrue(component != null && component.termites().size() == 2, "component " + component);

        var encoded = TermitesComponent.CODEC.encodeStart(NbtOps.INSTANCE, component).getOrThrow();
        TermitesComponent decoded = TermitesComponent.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        h.assertTrue(decoded.termites().size() == 2, "codec round trip");
        h.assertTrue(decoded.termites().getFirst().loadEntity(h.getLevel()) instanceof Termite, "stored entity should load as a termite");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void emergencyReleasesEverything(GameTestHelper h) {
        TermitehiveBlockEntity be = hive(h, AT);
        be.addTermite(parked());
        be.addTermite(parked());
        be.angerTermites(TermitehiveBlockEntity.TermiteState.EMERGENCY);
        h.assertTrue(be.hasNoTermites(), "an emergency empties the hive");
        h.assertEntityPresent(TermiteRegistry.TERMITE);
        h.succeed();
    }

    @GameTest(template = "platform", batch = "termite_night", timeoutTicks = 300)
    public static void termiteGoesHomeAtNight(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        long time = level.getDayTime();
        level.setDayTime(18000);
        TermitehiveBlockEntity be = hive(h, AT);
        Termite termite = h.spawn(TermiteRegistry.TERMITE, AT.east(2));
        h.succeedWhen(() -> {
            h.assertTrue(termite.isRemoved(), "termite should be inside by now");
            h.assertTrue(be.getTermiteCount() == 1, "hive should hold it");
            level.setDayTime(time);
        });
    }

    @GameTest(template = "platform", timeoutTicks = 800)
    public static void termiteHollowsLogsByDay(GameTestHelper h) {
        List<BlockPos> ring = BlockPos.betweenClosedStream(AT.offset(-1, 0, -1), AT.offset(1, 1, 1))
                .map(BlockPos::immutable)
                .filter(p -> p.getX() != AT.getX() || p.getZ() != AT.getZ())
                .toList();
        ring.forEach(p -> h.setBlock(p, Blocks.OAK_LOG));
        h.spawn(TermiteRegistry.TERMITE, AT);
        h.succeedWhen(() -> h.assertTrue(ring.stream().anyMatch(p -> h.getBlockState(p).is(BlockRegistry.HOLLOW_OAK_LOG)),
                "no log was hollowed"));
    }

    @GameTest(template = "platform", timeoutTicks = 100)
    public static void termiteSwipesAndSettles(GameTestHelper h) {
        Termite termite = h.spawn(TermiteRegistry.TERMITE, AT);
        Zombie target = h.spawn(EntityType.ZOMBIE, AT.east());
        termite.doHurtTarget(target);
        h.assertTrue(termite.swipeAnimationState.isStarted(), "hit should start the swipe");
        h.assertTrue(termite.getAttributeValue(Attributes.ATTACK_DAMAGE) == 2.0, "attack damage");
        h.assertTrue(termite.getBbHeight() == 0.5F, "height");
        h.succeedWhen(() -> h.assertTrue(!termite.swipeAnimationState.isStarted(), "swipe should finish after a second"));
    }

    @GameTest(template = "platform")
    public static void spawnOptionBlocksTermites(GameTestHelper h) {
        ModConfigValues.spawnTermite.set(false);
        try {
            Termite termite = TermiteRegistry.TERMITE.create(h.getLevel());
            termite.moveTo(h.absoluteVec(Vec3.atCenterOf(AT)));
            h.assertTrue(!h.getLevel().addFreshEntity(termite), "disabled termites must not join the world");
        } finally {
            ModConfigValues.spawnTermite.reset();
        }
        Termite termite = TermiteRegistry.TERMITE.create(h.getLevel());
        termite.moveTo(h.absoluteVec(Vec3.atCenterOf(AT)));
        h.assertTrue(h.getLevel().addFreshEntity(termite), "enabled termites join the world");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void moundFeaturePlacesHivesWithTermites(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        var biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        for (var biome : List.of(Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA, Biomes.DESERT)) {
            var local = biomes.getHolderOrThrow(biome).value().getGenerationSettings().features()
                    .get(GenerationStep.Decoration.LOCAL_MODIFICATIONS.ordinal());
            h.assertTrue(local.stream().anyMatch(f -> f.is(ModFeatures.MOUND_KEY)), "mound missing from " + biome.location());
        }

        ConfiguredFeature<?, ?> mound = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).get(NekomasFixed.id("mound"));
        h.assertTrue(mound != null, "configured feature missing");

        // Built high above the test floor, then cleared in the same tick, so that neighbouring tests are left alone.
        BlockPos origin = h.absolutePos(AT).above(30);
        List<BlockPos> area = BlockPos.betweenClosedStream(origin.offset(-5, -1, -5), origin.offset(5, 8, 5)).map(BlockPos::immutable).toList();
        for (BlockPos p : area) {
            if (p.getY() == origin.getY() - 1) level.setBlock(p, Blocks.STONE.defaultBlockState(), 2);
        }
        var generator = level.getChunkSource().getGenerator();

        ModConfigValues.generateMound.set(false);
        boolean placedWhenOff = mound.place(level, generator, RandomSource.create(1), origin);
        ModConfigValues.generateMound.reset();
        h.assertTrue(!placedWhenOff && level.getBlockState(origin).isAir(), "generateMound=false must place nothing");

        h.assertTrue(mound.place(level, generator, RandomSource.create(42), origin), "mound should place");
        int hives = 0;
        int blocks = 0;
        int termites = 0;
        for (BlockPos p : area) {
            BlockState state = level.getBlockState(p);
            if (state.is(TermiteRegistry.TERMITE_BLOCK)) blocks++;
            if (state.is(TermiteRegistry.TERMITE_HIVE)) {
                hives++;
                termites += ((TermitehiveBlockEntity) level.getBlockEntity(p)).getTermiteCount();
            }
        }
        for (BlockPos p : area) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
        }
        h.assertTrue(hives > 0 && blocks > 0, "mound had " + hives + " hives and " + blocks + " blocks");
        h.assertTrue(termites >= hives, "every hive holds at least one termite: " + termites + " in " + hives);
        h.succeed();
    }

    private static ItemStack recipeResult(GameTestHelper h, String path) {
        RecipeHolder<?> holder = h.getLevel().getRecipeManager().byKey(NekomasFixed.id(path)).orElse(null);
        h.assertTrue(holder != null, "recipe missing: " + path);
        return holder.value().getResultItem(h.getLevel().registryAccess());
    }

    @GameTest(template = "platform")
    public static void craftingRecipesGiveTheirResults(GameTestHelper h) {
        for (Item crown : CrownRegistry.FLOWER_CROWNS) {
            String path = BuiltInRegistries.ITEM.getKey(crown).getPath();
            h.assertTrue(recipeResult(h, path).is(crown), "wrong result for " + path);
            RecipeHolder<?> holder = h.getLevel().getRecipeManager().byKey(NekomasFixed.id(path)).orElseThrow();
            FlowerCrownItem item = (FlowerCrownItem) crown;
            h.assertTrue(holder.value() instanceof CraftingRecipe recipe
                            && recipe.getIngredients().stream().anyMatch(i -> i.test(item.getVariant().getFlower().getDefaultInstance())),
                    path + " should use its flower");
        }
        for (Item helmet : List.of(GoatHornRegistry.GOAT_HORN_IRON_HELMET, GoatHornRegistry.GOAT_HORN_GOLDEN_HELMET,
                GoatHornRegistry.GOAT_HORN_DIAMOND_HELMET, GoatHornRegistry.GOAT_HORN_NETHERITE_HELMET, GoatHornRegistry.GOAT_HORN_TURTLE_HELMET)) {
            String path = BuiltInRegistries.ITEM.getKey(helmet).getPath();
            h.assertTrue(recipeResult(h, path).is(helmet), "wrong result for " + path);
        }
        ItemStack crownTemplates = recipeResult(h, "crown_smithing_template");
        h.assertTrue(crownTemplates.is(CrownRegistry.CROWN_SMITHING_TEMPLATE) && crownTemplates.getCount() == 2, "crown template duplicate");
        ItemStack jewelTemplates = recipeResult(h, "jewel_armor_trim_smithing_template");
        h.assertTrue(jewelTemplates.is(TrimRegistry.JEWEL_ARMOR_TRIM_SMITHING_TEMPLATE) && jewelTemplates.getCount() == 2, "jewel template duplicate");
        h.succeed();
    }

    // The crown recipes wait for the wildfire's nether heart, so here they are also decoded with a stand-in for it.
    @GameTest(template = "platform")
    public static void crownSmithingRecipesDecode(GameTestHelper h) throws Exception {
        var server = h.getLevel().getServer();
        var ops = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
        boolean heart = BuiltInRegistries.ITEM.containsKey(NekomasFixed.id("nether_heart"));
        for (Item crown : List.of(CrownRegistry.COPPER_CROWN, CrownRegistry.IRON_CROWN, CrownRegistry.GOLDEN_CROWN, CrownRegistry.DIAMOND_CROWN, CrownRegistry.NETHERITE_CROWN)) {
            String path = BuiltInRegistries.ITEM.getKey(crown).getPath() + "_smithing";
            String text;
            try (Reader reader = server.getResourceManager().getResourceOrThrow(NekomasFixed.id("recipe/" + path + ".json")).openAsReader()) {
                text = JsonParser.parseReader(reader).toString();
            }
            JsonElement json = JsonParser.parseString(text.replace("nekomasfixed:nether_heart", "minecraft:nether_star"));
            Recipe<?> recipe = Recipe.CODEC.parse(ops, json).getOrThrow();
            h.assertTrue(recipe.getResultItem(server.registryAccess()).is(crown), "wrong result for " + path);
            h.assertTrue(heart == h.getLevel().getRecipeManager().byKey(NekomasFixed.id(path)).isPresent(),
                    path + " should load exactly when the nether heart exists");
        }
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void jewelTrimLoadsAndApplies(GameTestHelper h) {
        var registries = h.getLevel().registryAccess();
        var patterns = registries.registryOrThrow(Registries.TRIM_PATTERN);
        var jewel = patterns.getHolderOrThrow(TrimRegistry.JEWEL);
        h.assertTrue(jewel.value().templateItem().value() == TrimRegistry.JEWEL_ARMOR_TRIM_SMITHING_TEMPLATE, "template item");
        h.assertTrue(jewel.value().assetId().equals(NekomasFixed.id("jewel")), "asset id");

        for (Item base : List.of(CrownRegistry.IRON_CROWN, GoatHornRegistry.GOAT_HORN_DIAMOND_HELMET, Items.NETHERITE_CHESTPLATE)) {
            h.assertTrue(base.getDefaultInstance().is(ItemTags.TRIMMABLE_ARMOR), base + " should be trimmable");
            RecipeHolder<?> holder = h.getLevel().getRecipeManager().byKey(NekomasFixed.id("jewel_armor_trim_smithing_trim")).orElseThrow();
            @SuppressWarnings("unchecked")
            Recipe<SmithingRecipeInput> recipe = (Recipe<SmithingRecipeInput>) holder.value();
            SmithingRecipeInput input = new SmithingRecipeInput(TrimRegistry.JEWEL_ARMOR_TRIM_SMITHING_TEMPLATE.getDefaultInstance(),
                    base.getDefaultInstance(), Items.DIAMOND.getDefaultInstance());
            h.assertTrue(recipe.matches(input, h.getLevel()), "trim recipe should match " + base);
            ArmorTrim trim = recipe.assemble(input, registries).get(DataComponents.TRIM);
            h.assertTrue(trim != null && trim.pattern().is(TrimRegistry.JEWEL), "trim on " + base + " was " + trim);
        }
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void wearablesHaveTheirStats(GameTestHelper h) {
        record Stats(Item item, int defense, int durability) {}
        for (Stats s : List.of(
                new Stats(CrownRegistry.COPPER_CROWN, 2, 121),
                new Stats(CrownRegistry.IRON_CROWN, 2, 165),
                new Stats(CrownRegistry.GOLDEN_CROWN, 2, 77),
                new Stats(CrownRegistry.DIAMOND_CROWN, 3, 363),
                new Stats(CrownRegistry.NETHERITE_CROWN, 3, 407),
                new Stats(GoatHornRegistry.GOAT_HORN_IRON_HELMET, 2, 165),
                new Stats(GoatHornRegistry.GOAT_HORN_GOLDEN_HELMET, 2, 77),
                new Stats(GoatHornRegistry.GOAT_HORN_DIAMOND_HELMET, 3, 363),
                new Stats(GoatHornRegistry.GOAT_HORN_NETHERITE_HELMET, 3, 407),
                new Stats(GoatHornRegistry.GOAT_HORN_TURTLE_HELMET, 2, 275))) {
            ArmorItem armor = (ArmorItem) s.item();
            ItemStack stack = armor.getDefaultInstance();
            h.assertTrue(armor.getEquipmentSlot() == EquipmentSlot.HEAD, armor + " slot");
            h.assertTrue(armor.getDefense() == s.defense(), armor + " defense " + armor.getDefense());
            h.assertTrue(stack.getMaxDamage() == s.durability(), armor + " durability " + stack.getMaxDamage());
            h.assertTrue(stack.is(ItemTags.HEAD_ARMOR), armor + " should be head armor");
        }
        h.assertTrue(CrownRegistry.IRON_CROWN instanceof CrownItem, "crown item");
        h.assertTrue(CrownRegistry.NETHERITE_CROWN.getDefaultInstance().has(DataComponents.FIRE_RESISTANT), "netherite crown survives fire");
        h.assertTrue(GoatHornRegistry.GOAT_HORN_NETHERITE_HELMET.getDefaultInstance().has(DataComponents.FIRE_RESISTANT), "netherite horn helmet survives fire");
        h.assertTrue(CrownRegistry.COPPER_CROWN.isValidRepairItem(CrownRegistry.COPPER_CROWN.getDefaultInstance(), Items.COPPER_INGOT.getDefaultInstance()), "copper crown repairs with copper");
        h.assertTrue(GoatHornRegistry.GOAT_HORN_IRON_HELMET instanceof GoatHornHelmetItem, "helmet item");
        h.assertTrue(GoatHornRegistry.GOAT_HORN_IRON_HELMET.getDefaultInstance().is(ModTags.GOAT_HORN_HELMETS), "goat horn tag");

        for (Item crown : CrownRegistry.FLOWER_CROWNS) {
            ItemStack stack = crown.getDefaultInstance();
            h.assertTrue(stack.is(ModTags.FLOWER_CROWNS), crown + " tag");
            h.assertTrue(stack.is(ItemTags.HEAD_ARMOR), crown + " head armor tag");
            h.assertTrue(stack.getMaxStackSize() == 1, crown + " stack");
            Zombie zombie = h.spawn(EntityType.ZOMBIE, AT);
            h.assertTrue(zombie.getEquipmentSlotForItem(stack) == EquipmentSlot.HEAD, crown + " should go on the head");
            zombie.discard();
        }

        for (Item template : List.of(CrownRegistry.CROWN_SMITHING_TEMPLATE, TrimRegistry.JEWEL_ARMOR_TRIM_SMITHING_TEMPLATE)) {
            ItemStack stack = template.getDefaultInstance();
            DamageSource explosion = h.getLevel().damageSources().explosion(null, null);
            DamageSource fire = h.getLevel().damageSources().inFire();
            h.assertTrue(!stack.canBeHurtBy(explosion), template + " survives explosions");
            h.assertTrue(!stack.canBeHurtBy(fire), template + " survives fire");
            h.assertTrue(stack.getRarity() == net.minecraft.world.item.Rarity.UNCOMMON, template + " rarity");
        }
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void flowerCrownCanAffectItsWearerWhenHit(GameTestHelper h) {
        Zombie wearer = h.spawn(EntityType.ZOMBIE, AT);
        wearer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(CrownRegistry.POPPY_FLOWER_CROWN));
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(wearer.position());
        for (int i = 0; i < 64 && !wearer.hasEffect(MobEffects.NIGHT_VISION); i++) {
            wearer.setHealth(wearer.getMaxHealth());
            player.attack(wearer);
        }
        h.assertTrue(wearer.hasEffect(MobEffects.NIGHT_VISION), "poppy crown should have given night vision");
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void goatHornHelmetThrowsWhatItTouches(GameTestHelper h) {
        Zombie zombie = h.spawn(EntityType.ZOMBIE, AT);
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        ItemStack helmet = new ItemStack(GoatHornRegistry.GOAT_HORN_IRON_HELMET);
        player.setItemSlot(EquipmentSlot.HEAD, helmet);
        player.setXRot(60);
        h.runAfterDelay(90, () -> {
            zombie.setDeltaMovement(Vec3.ZERO);
            zombie.playerTouch(player);
            h.assertTrue(zombie.getDeltaMovement().horizontalDistance() > 1.0, "zombie should be tossed, got " + zombie.getDeltaMovement());
            h.assertTrue(player.getItemBySlot(EquipmentSlot.HEAD).getDamageValue() == 1, "toss wears the helmet");
            zombie.setDeltaMovement(Vec3.ZERO);
            zombie.playerTouch(player);
            h.assertTrue(zombie.getDeltaMovement().horizontalDistance() < 0.1, "the next toss waits for the cooldown");
            h.succeed();
        });
    }

    @GameTest(template = "platform")
    public static void goatHornStoresAndGivesBackTorches(GameTestHelper h) {
        h.setBlock(AT.north(), Blocks.STONE);
        h.setBlock(AT, BlockRegistry.GOAT_HORN.defaultBlockState().setValue(GoatHornBlock.FACING, Direction.NORTH));
        BlockPos abs = h.absolutePos(AT);
        ServerLevel level = h.getLevel();
        GoatHornBlockEntity be = (GoatHornBlockEntity) level.getBlockEntity(abs);
        h.assertTrue(be != null, "horn should have a block entity");

        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);

        ItemStack stick = new ItemStack(Items.STICK, 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, stick);
        level.getBlockState(abs).useItemOn(stick, level, player, InteractionHand.MAIN_HAND, hit);
        h.assertTrue(stick.getCount() == 3, "a horn only takes torches");

        ItemStack torch = new ItemStack(Items.SOUL_TORCH, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, torch);
        level.getBlockState(abs).useItemOn(torch, level, player, InteractionHand.MAIN_HAND, hit);
        h.assertTrue(torch.getCount() == 1, "torch should be used up");
        h.assertBlockProperty(AT, GoatHornBlock.TORCH, GoatHornTorchType.SOUL_TORCH);
        h.assertTrue(be.getTorch().is(Blocks.SOUL_TORCH), "stored torch " + be.getTorch());

        var registries = level.registryAccess();
        GoatHornBlockEntity loaded = new GoatHornBlockEntity(abs, level.getBlockState(abs));
        loaded.loadWithComponents(be.saveWithoutMetadata(registries), registries);
        h.assertTrue(loaded.getTorch().is(Blocks.SOUL_TORCH), "torch should save, got " + loaded.getTorch());

        ItemStack shears = new ItemStack(Items.SHEARS);
        player.setItemInHand(InteractionHand.MAIN_HAND, shears);
        level.getBlockState(abs).useItemOn(shears, level, player, InteractionHand.MAIN_HAND, hit);
        h.assertBlockProperty(AT, GoatHornBlock.TORCH, GoatHornTorchType.NONE);
        h.assertTrue(be.getTorch().isAir(), "torch should be gone");
        h.assertItemEntityPresent(Items.SOUL_TORCH, AT, 2.0);
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void blowingAHornCallsCompanions(GameTestHelper h) {
        Wolf wolf = h.spawn(EntityType.WOLF, AT);
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        wolf.tame(player);
        IronGolem golem = h.spawn(EntityType.IRON_GOLEM, AT.east(2));
        player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 600));

        var ponder = BuiltInRegistries.INSTRUMENT.getHolderOrThrow(Instruments.PONDER_GOAT_HORN);
        ItemStack horn = InstrumentItem.create(Items.GOAT_HORN, ponder);
        player.setItemInHand(InteractionHand.MAIN_HAND, horn);
        horn.use(h.getLevel(), player, InteractionHand.MAIN_HAND);

        h.assertTrue(wolf.hasEffect(MobEffects.GLOWING), "tame wolf should glow");
        h.assertTrue(golem.hasEffect(MobEffects.DAMAGE_RESISTANCE), "golem should get the horn's effect");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void advancementsLoad(GameTestHelper h) {
        var advancements = h.getLevel().getServer().getAdvancements();
        for (String path : List.of("flower_power", "i_am_the_ruler", "i_am_on_fire_now", "spooky")) {
            ResourceLocation id = NekomasFixed.id("mod/" + path);
            h.assertTrue(advancements.tree().get(id) != null, "advancement missing from the tree: " + id);
        }
        h.succeed();
    }
}
