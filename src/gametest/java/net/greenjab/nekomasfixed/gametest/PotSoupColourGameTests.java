package net.greenjab.nekomasfixed.gametest;

import it.unimi.dsi.fastutil.ints.IntList;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.mixin.accessor.SnifferAccessor;
import net.greenjab.nekomasfixed.registry.block.SulfurFireBlock;
import net.greenjab.nekomasfixed.registry.block.entity.SoupCauldronBlockEntity;
import net.greenjab.nekomasfixed.registry.block.entity.TerracottaDecoratedPotBlockEntity;
import net.greenjab.nekomasfixed.registry.item.DyedBrushItem;
import net.greenjab.nekomasfixed.registry.other.PotEngravingDecoration;
import net.greenjab.nekomasfixed.registry.other.PotFaceDecoration;
import net.greenjab.nekomasfixed.registry.registries.BrushRegistry;
import net.greenjab.nekomasfixed.registry.registries.ComponentRegistry;
import net.greenjab.nekomasfixed.registry.registries.ItemRegistry;
import net.greenjab.nekomasfixed.registry.registries.PotInteractions;
import net.greenjab.nekomasfixed.registry.registries.PotRegistry;
import net.greenjab.nekomasfixed.registry.registries.SoupRegistry;
import net.greenjab.nekomasfixed.registry.registries.SulfurRegistry;
import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.greenjab.nekomasfixed.target_access_class.DecoratedPotAccess;
import net.greenjab.nekomasfixed.util.AllDyes;
import net.greenjab.nekomasfixed.util.BlockDyeMap;
import net.greenjab.nekomasfixed.util.ItemDyeMap;
import net.greenjab.nekomasfixed.util.MessyBedAccessor;
import net.greenjab.nekomasfixed.util.ModColors;
import net.greenjab.nekomasfixed.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(NekomasFixed.NAMESPACE)
@PrefixGameTestTemplate(false)
public class PotSoupColourGameTests {
    private static final BlockPos AT = new BlockPos(1, 2, 1);

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(NekomasFixed.id(id));
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(NekomasFixed.id(id));
    }

    private static ItemStack craft(GameTestHelper h, int width, int height, ItemStack... slots) {
        ServerLevel level = h.getLevel();
        CraftingInput input = CraftingInput.of(width, height, List.of(slots));
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(r -> r.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
    }

    private static ItemStack s(Item item) {
        return new ItemStack(item);
    }

    private static ItemStack craftRing(GameTestHelper h, Item ring, Item middle) {
        return craft(h, 3, 3, s(ring), s(ring), s(ring), s(ring), s(middle), s(ring), s(ring), s(ring), s(ring));
    }

    private static BlockHitResult hit(GameTestHelper h, BlockPos relative, Direction side) {
        BlockPos pos = h.absolutePos(relative);
        return new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false);
    }

    @GameTest(template = "platform")
    public static void everyColourHasItsBlocks(GameTestHelper h) {
        List<String> missing = new ArrayList<>();
        for (AllDyes dye : AllDyes.values()) {
            for (String family : List.of("_wool_slab", "_wool_stairs", "_spotted_wool_slab", "_spotted_wool_stairs", "_concrete_slab", "_concrete_stairs")) {
                String id = dye.getSerializedName() + family;
                if (block(id) == Blocks.AIR || item(id) == Items.AIR) missing.add(id);
            }
            if (!(item(dye.getSerializedName() + "_dyed_brush") instanceof DyedBrushItem)) missing.add(dye + " brush");
            for (var map : List.of(BlockDyeMap.WOOL, BlockDyeMap.CARPET, BlockDyeMap.BED, BlockDyeMap.SHULKER_BOX, BlockDyeMap.CANDLE, BlockDyeMap.CONCRETE,
                    BlockDyeMap.CONCRETE_POWDER, BlockDyeMap.TERRACOTTA, BlockDyeMap.GLAZED_TERRACOTTA, BlockDyeMap.STAINED_GLASS, BlockDyeMap.STAINED_GLASS_PANE,
                    BlockDyeMap.WOOL_SLABS, BlockDyeMap.WOOL_STAIRS, BlockDyeMap.SPOTTED_WOOL_SLABS, BlockDyeMap.SPOTTED_WOOL_STAIRS, BlockDyeMap.CONCRETE_SLABS, BlockDyeMap.CONCRETE_STAIRS)) {
                if (map.get(dye) == null) missing.add(dye + " in a dye map");
            }
            if (ItemDyeMap.BRUSH.get(dye) == null || ItemDyeMap.DYE.get(dye) == null) missing.add(dye + " in the item map");
        }
        h.assertTrue(missing.isEmpty(), "missing " + missing);
        BlockState slab = BlockDyeMap.WOOL_SLABS.get(AllDyes.RED).defaultBlockState();
        h.assertTrue(slab.is(BlockTags.SLABS) && slab.is(ModTags.WOOL_SLABS) && slab.is(ModTags.CAN_BE_DYED_WITH_BRUSH), "red wool slab tags");
        h.assertTrue(BlockDyeMap.CONCRETE_STAIRS.get(AllDyes.AQUA).defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE), "concrete stairs need a pickaxe");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void slabAndStairRecipes(GameTestHelper h) {
        ItemStack redSlabs = craft(h, 3, 1, s(Items.RED_WOOL), s(Items.RED_WOOL), s(Items.RED_WOOL));
        h.assertTrue(redSlabs.is(item("red_wool_slab")) && redSlabs.getCount() == 6, "red wool slab: " + redSlabs);
        ItemStack amberSlabs = craft(h, 3, 1, s(ItemRegistry.AMBER_WOOL), s(ItemRegistry.AMBER_WOOL), s(ItemRegistry.AMBER_WOOL));
        h.assertTrue(amberSlabs.is(item("amber_wool_slab")) && amberSlabs.getCount() == 6, "amber wool slab: " + amberSlabs);
        Item spotted = item("black_spotted_wool");
        ItemStack spottedSlabs = craft(h, 3, 1, s(spotted), s(spotted), s(spotted));
        h.assertTrue(spottedSlabs.is(item("black_spotted_wool_slab")), "spotted wool slab: " + spottedSlabs);
        ServerLevel level = h.getLevel();
        ItemStack stairs = level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, new SingleRecipeInput(s(Items.BLUE_CONCRETE)), level).stream()
                .map(r -> r.value().assemble(new SingleRecipeInput(s(Items.BLUE_CONCRETE)), level.registryAccess()))
                .filter(r -> r.is(item("blue_concrete_stairs"))).findFirst().orElse(ItemStack.EMPTY);
        h.assertTrue(!stairs.isEmpty(), "blue concrete stairs stonecutting");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void potRecipeMakesTheTerracottaPot(GameTestHelper h) {
        ItemStack e = ItemStack.EMPTY;
        ItemStack pot = craft(h, 3, 3, e, s(Items.BLADE_POTTERY_SHERD), e, s(Items.BRICK), e, s(Items.SKULL_POTTERY_SHERD), e, s(Items.PRIZE_POTTERY_SHERD), e);
        h.assertTrue(pot.is(PotRegistry.TERRACOTTA_DECORATED_POT_ITEM), "pot recipe gave " + pot);
        PotDecorations decorations = pot.get(DataComponents.POT_DECORATIONS);
        h.assertTrue(decorations.back().orElse(null) == Items.BLADE_POTTERY_SHERD && decorations.front().orElse(null) == Items.PRIZE_POTTERY_SHERD
                && decorations.left().isEmpty() && decorations.right().orElse(null) == Items.SKULL_POTTERY_SHERD, "pot sherds " + decorations);
        h.assertTrue(pot.get(PotRegistry.POT_FACE_BLOCK).getSafeBlock() == Blocks.BRICKS, "pot face");
        h.succeed();
    }

    private static TerracottaDecoratedPotBlockEntity placePot(GameTestHelper h, PotDecorations decorations) {
        h.setBlock(AT, PotRegistry.TERRACOTTA_DECORATED_POT);
        TerracottaDecoratedPotBlockEntity pot = h.getBlockEntity(AT);
        pot.applyComponentsFromItemStack(TerracottaDecoratedPotBlockEntity.createDecoratedPotInstance(decorations, PotEngravingDecoration.EMPTY, PotFaceDecoration.FALLBACK_BRICKS));
        return pot;
    }

    private static Player crouching(GameTestHelper h, ItemStack held) {
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        player.setPose(Pose.CROUCHING);
        return player;
    }

    @GameTest(template = "platform")
    public static void potEngravesAndChangesFace(GameTestHelper h) {
        TerracottaDecoratedPotBlockEntity pot = placePot(h, new PotDecorations(Items.BLADE_POTTERY_SHERD, Items.BRICK, Items.BRICK, Items.BRICK));
        Direction facing = pot.getDirection();
        ServerLevel level = h.getLevel();

        // The pot is placed facing north: the blade sherd is the back side, on the south.
        h.assertTrue(facing == Direction.NORTH, "pot direction " + facing);
        ItemStack iron = new ItemStack(Items.IRON_INGOT, 2);
        Player player = crouching(h, iron);
        var event = CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, h.absolutePos(AT), hit(h, AT, Direction.SOUTH));
        h.assertTrue(event.isCanceled(), "engraving through the use event");
        h.assertTrue(pot.getEngravingDecorations().getBack() == Items.IRON_INGOT && iron.getCount() == 1, "engraved " + pot.getEngravingDecorations());

        // A side with no sherd can't be engraved, the same item twice neither.
        h.assertTrue(PotInteractions.interact(player, level, iron, InteractionHand.MAIN_HAND, hit(h, AT, Direction.NORTH)) == InteractionResult.PASS, "empty side");
        h.assertTrue(PotInteractions.interact(player, level, iron, InteractionHand.MAIN_HAND, hit(h, AT, Direction.SOUTH)) == InteractionResult.FAIL, "same engraving");

        // Shears take it off and drop it.
        ItemStack shears = new ItemStack(Items.SHEARS);
        Player shearing = crouching(h, shears);
        h.assertTrue(PotInteractions.interact(shearing, level, shears, InteractionHand.MAIN_HAND, hit(h, AT, Direction.SOUTH)).consumesAction(), "shears");
        h.assertTrue(pot.getEngravingDecorations().equals(PotEngravingDecoration.EMPTY), "engraving removed");
        h.assertItemEntityPresent(Items.IRON_INGOT, AT, 2.0);

        // A face block swaps the face and drops the old one.
        ItemStack glazed = new ItemStack(Items.LIGHT_BLUE_GLAZED_TERRACOTTA);
        Player facePlayer = crouching(h, glazed);
        h.assertTrue(PotInteractions.interact(facePlayer, level, glazed, InteractionHand.MAIN_HAND, hit(h, AT, Direction.EAST)).consumesAction(), "face");
        h.assertTrue(pot.getPotFace().getSafeBlock() == Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA && glazed.isEmpty(), "new face");
        h.assertItemEntityPresent(Items.BRICKS, AT, 2.0);

        // Standing up nothing happens here: it goes on to the pot, which stores the item.
        Player standing = h.makeMockPlayer(GameType.SURVIVAL);
        h.assertTrue(PotInteractions.interact(standing, level, iron, InteractionHand.MAIN_HAND, hit(h, AT, Direction.SOUTH)) == InteractionResult.PASS, "standing");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void potSavesLoadsAndStoresItems(GameTestHelper h) {
        TerracottaDecoratedPotBlockEntity pot = placePot(h, new PotDecorations(Items.BLADE_POTTERY_SHERD, Items.BRICK, Items.MINER_POTTERY_SHERD, Items.BRICK));
        ServerLevel level = h.getLevel();
        pot.engravingDecorations = pot.getEngravingDecorations().engraveSideFacing(Direction.SOUTH, Direction.NORTH, Items.DIAMOND);
        pot.faceDecoration = new PotFaceDecoration(Blocks.ORANGE_TERRACOTTA.defaultBlockState());
        pot.nekomasfixed$setSherdGlow(DecoratedPotAccess.BACK, true);

        // A plain right click puts the item in, and it doesn't glow the side.
        ItemStack apple = new ItemStack(Items.APPLE, 3);
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, apple);
        var event = CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, h.absolutePos(AT), hit(h, AT, Direction.EAST));
        h.assertTrue(!event.isCanceled(), "a plain item goes on to the pot");
        h.assertTrue(!pot.nekomasfixed$getSherdGlow(DecoratedPotAccess.RIGHT), "only glow ink makes a side glow");
        level.getBlockState(pot.getBlockPos()).useItemOn(apple, level, player, InteractionHand.MAIN_HAND, hit(h, AT, Direction.EAST));
        h.assertTrue(pot.getTheItem().is(Items.APPLE) && apple.getCount() == 2, "stored " + pot.getTheItem());

        CompoundTag tag = pot.saveWithoutMetadata(level.registryAccess());
        TerracottaDecoratedPotBlockEntity loaded = new TerracottaDecoratedPotBlockEntity(pot.getBlockPos(), pot.getBlockState());
        loaded.loadWithComponents(tag, level.registryAccess());
        h.assertTrue(loaded.getDecorations().equals(pot.getDecorations()), "sherds " + tag);
        h.assertTrue(loaded.getEngravingDecorations().getBack() == Items.DIAMOND, "engraving " + tag);
        h.assertTrue(loaded.getPotFace().getSafeBlock() == Blocks.ORANGE_TERRACOTTA, "face " + tag);
        h.assertTrue(loaded.nekomasfixed$getSherdGlow(DecoratedPotAccess.BACK), "glow " + tag);
        h.assertTrue(loaded.getTheItem().is(Items.APPLE), "item " + tag);

        // Glow ink on the same path as the vanilla pot.
        ItemStack ink = new ItemStack(Items.GLOW_INK_SAC);
        Player inking = h.makeMockPlayer(GameType.SURVIVAL);
        inking.setItemInHand(InteractionHand.MAIN_HAND, ink);
        h.assertTrue(CommonHooks.onRightClickBlock(inking, InteractionHand.MAIN_HAND, h.absolutePos(AT), hit(h, AT, Direction.EAST)).isCanceled(), "glow ink is taken");
        h.assertTrue(pot.nekomasfixed$getSherdGlow(DecoratedPotAccess.RIGHT) && ink.isEmpty(), "glow ink makes the side glow");

        // The picked pot carries everything.
        ItemStack picked = PotRegistry.TERRACOTTA_DECORATED_POT.getCloneItemStack(level, pot.getBlockPos(), pot.getBlockState());
        h.assertTrue(picked.get(PotRegistry.POT_ENGRAVED_DECORATIONS).getBack() == Items.DIAMOND && picked.get(PotRegistry.POT_FACE_BLOCK).getSafeBlock() == Blocks.ORANGE_TERRACOTTA, "picked " + picked);

        // Vanilla pots still take glow ink and items the same way.
        h.setBlock(AT.east(), Blocks.DECORATED_POT);
        DecoratedPotBlockEntity vanilla = h.getBlockEntity(AT.east());
        ItemStack vanillaInk = new ItemStack(Items.GLOW_INK_SAC);
        Player vanillaPlayer = h.makeMockPlayer(GameType.SURVIVAL);
        vanillaPlayer.setItemInHand(InteractionHand.MAIN_HAND, vanillaInk);
        h.assertTrue(CommonHooks.onRightClickBlock(vanillaPlayer, InteractionHand.MAIN_HAND, h.absolutePos(AT.east()), hit(h, AT.east(), Direction.EAST)).isCanceled(), "vanilla pot glow ink");
        h.assertTrue(((DecoratedPotAccess) vanilla).nekomasfixed$getSherdGlow(DecoratedPotAccess.RIGHT), "vanilla pot glows");
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 300)
    public static void snifferDigsFromTheBiomeTables(GameTestHelper h) {
        for (String name : List.of("sniffer_digging", "sniffer_digging_snowy", "sniffer_digging_desert", "sniffer_digging_badlands")) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("gameplay/" + name));
            h.assertTrue(h.getLevel().getServer().reloadableRegistries().getLootTable(key) != LootTable.EMPTY, name + " loot table");
        }
        Sniffer sniffer = h.spawnWithNoFreeWill(EntityType.SNIFFER, new BlockPos(1, 2, 1));
        sniffer.transitionTo(Sniffer.State.DIGGING);
        h.assertTrue(sniffer.getEntityData().get(SnifferAccessor.getFinishDigTime()) > sniffer.tickCount, "digging started");
        h.succeedWhen(() -> h.assertTrue(!h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(AT)).inflate(4)).isEmpty(), "sniffer dropped nothing yet"));
    }

    private static void placeHeatedCauldron(GameTestHelper h) {
        h.setBlock(AT, Blocks.CAMPFIRE);
        h.setBlock(AT.above(), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
    }

    private static void use(GameTestHelper h, Player player, ItemStack stack, BlockPos relative) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos pos = h.absolutePos(relative);
        h.getLevel().getBlockState(pos).useItemOn(stack, h.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    @GameTest(template = "platform")
    public static void soupStartsCooksAndServes(GameTestHelper h) {
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        BlockPos cauldron = AT.above();

        // No fire, no soup.
        h.setBlock(cauldron, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        use(h, player, new ItemStack(Items.APPLE), cauldron);
        h.assertBlockPresent(Blocks.WATER_CAULDRON, cauldron);

        placeHeatedCauldron(h);
        ItemStack beef = new ItemStack(Items.BEEF, 2);
        use(h, player, beef, cauldron);
        h.assertBlockPresent(SoupRegistry.SOUP_CAULDRON, cauldron);
        h.assertTrue(beef.getCount() == 1, "beef used " + beef);
        SoupCauldronBlockEntity soup = h.getBlockEntity(cauldron);
        h.assertTrue(soup.getInputs().size() == 1 && soup.getInputs().getFirst().is(Items.BEEF), "inputs " + soup.getInputs());

        // The same ingredient twice stays out.
        use(h, player, beef, cauldron);
        h.assertTrue(beef.getCount() == 1 && soup.getInputs().size() == 1, "second beef");
        use(h, player, new ItemStack(Items.CARROT), cauldron);
        h.assertTrue(soup.getInputs().size() == 2, "carrot in");

        // A bowl before stirring does nothing, an empty hand takes the last one back out.
        ItemStack bowl = new ItemStack(Items.BOWL);
        use(h, player, bowl, cauldron);
        h.assertBlockPresent(SoupRegistry.SOUP_CAULDRON, cauldron);
        use(h, player, ItemStack.EMPTY, cauldron);
        h.assertTrue(soup.getInputs().size() == 1 && player.getMainHandItem().is(Items.CARROT), "took the carrot back");
        use(h, player, new ItemStack(Items.MILK_BUCKET), cauldron);

        // A comparator reads half the cooked food.
        h.assertTrue(Blocks.AIR.defaultBlockState().isAir(), "");

        // Stirring cooks it.
        use(h, player, new ItemStack(Items.STICK), cauldron);
        h.assertTrue(soup.hasStirred && soup.getInputs().getFirst().is(Items.COOKED_BEEF), "cooked " + soup.getInputs());
        int signal = h.getLevel().getBlockState(soup.getBlockPos()).getAnalogOutputSignal(h.getLevel(), soup.getBlockPos());
        h.assertTrue(signal == 4, "comparator " + signal);

        // No more ingredients once stirred.
        ItemStack apple = new ItemStack(Items.APPLE);
        use(h, player, apple, cauldron);
        h.assertTrue(apple.getCount() == 1, "apple after stirring");

        // The bowl serves it: the cauldron empties and the milk bucket comes back.
        ItemStack bowlAgain = new ItemStack(Items.BOWL);
        use(h, player, bowlAgain, cauldron);
        h.assertBlockPresent(Blocks.CAULDRON, cauldron);
        ItemStack stew = player.getMainHandItem();
        h.assertTrue(stew.is(SoupRegistry.SPECIAL_STEW), "got " + stew);
        h.assertItemEntityPresent(Items.BUCKET, cauldron, 2.0);
        h.assertTrue(stew.get(DataComponents.DYED_COLOR) != null, "stew colour");
        h.assertTrue(stew.get(DataComponents.CONTAINER).nonEmptyStream().count() == 2, "stew ingredients");

        // Eating it: half of the cooked beef and a bowl back.
        player.getFoodData().setFoodLevel(5);
        player.getFoodData().setSaturation(0);
        ItemStack left = stew.getItem().finishUsingItem(stew, h.getLevel(), player);
        h.assertTrue(player.getFoodData().getFoodLevel() == 9, "food after stew " + player.getFoodData().getFoodLevel());
        h.assertTrue(left.is(Items.BOWL), "left " + left);
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void soupSavesAndLoads(GameTestHelper h) {
        placeHeatedCauldron(h);
        h.setBlock(AT.above(), SoupRegistry.SOUP_CAULDRON);
        SoupCauldronBlockEntity soup = h.getBlockEntity(AT.above());
        soup.addInput(new ItemStack(Items.APPLE));
        soup.addInput(new ItemStack(Items.COD));
        soup.setStirred(h.getLevel());
        CompoundTag tag = soup.saveWithoutMetadata(h.getLevel().registryAccess());
        SoupCauldronBlockEntity loaded = new SoupCauldronBlockEntity(soup.getBlockPos(), soup.getBlockState());
        loaded.loadWithComponents(tag, h.getLevel().registryAccess());
        h.assertTrue(loaded.hasStirred && loaded.getInputs().size() == 2 && loaded.getInputs().get(1).is(Items.COOKED_COD), "loaded " + tag);
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void sulfurLightsAndRecipes(GameTestHelper h) {
        ItemStack e = ItemStack.EMPTY;
        ItemStack torches = craft(h, 1, 2, s(Items.GUNPOWDER), s(Items.STICK));
        h.assertTrue(torches.is(SulfurRegistry.SULFUR_TORCH_ITEM) && torches.getCount() == 4, "sulfur torch " + torches);
        Item n = Items.GOLD_NUGGET;
        ItemStack lantern = craftRing(h, n, Items.GUNPOWDER);
        h.assertTrue(lantern.is(SulfurRegistry.SULFUR_LANTERN_ITEM), "sulfur lantern " + lantern);
        ItemStack chain = craft(h, 1, 3, s(n), s(Items.GOLD_INGOT), s(n));
        h.assertTrue(chain.is(SulfurRegistry.GOLD_CHAIN_ITEM) && chain.getCount() == 4, "gold chain " + chain);
        ItemStack campfire = craft(h, 3, 3, e, s(Items.STICK), e, s(Items.STICK), s(Items.GUNPOWDER), s(Items.STICK), s(Items.OAK_LOG), s(Items.OAK_LOG), s(Items.OAK_LOG));
        h.assertTrue(campfire.is(SulfurRegistry.SULFUR_CAMPFIRE_ITEM), "sulfur campfire " + campfire);

        h.setBlock(AT, Blocks.YELLOW_TERRACOTTA);
        BlockState fire = BaseFireBlock.getState(h.getLevel(), h.absolutePos(AT.above()));
        h.assertTrue(fire.getBlock() instanceof SulfurFireBlock, "fire on the sulfur base " + fire);
        h.setBlock(AT.above(), fire);
        h.assertBlockPresent(SulfurRegistry.SULFUR_FIRE, AT.above());
        h.setBlock(AT, Blocks.STONE);
        h.assertTrue(BaseFireBlock.getState(h.getLevel(), h.absolutePos(AT.above())).is(Blocks.FIRE), "plain fire elsewhere");

        h.setBlock(AT.east(), SulfurRegistry.SULFUR_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        BlockEntity be = h.getBlockEntity(AT.east());
        h.assertTrue(be instanceof CampfireBlockEntity, "campfire block entity " + be);
        h.assertTrue(SulfurRegistry.SULFUR_CAMPFIRE.defaultBlockState().is(BlockTags.CAMPFIRES), "campfires tag");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void brushRecipesFollowTheOption(GameTestHelper h) {
        Item amberBrush = item("amber_dyed_brush");
        Item whiteBrush = item("white_dyed_brush");
        h.assertTrue(craftRing(h, ItemRegistry.AMBER_DYE, Items.BRUSH).is(amberBrush), "amber brush recipe");
        h.assertTrue(craftRing(h, Items.WHITE_WOOL, Items.BRUSH).is(whiteBrush) || craftRing(h, Items.WHITE_DYE, Items.BRUSH).is(whiteBrush), "white brush recipe");
        h.assertTrue(h.getLevel().getRecipeManager().byKey(NekomasFixed.id("amber_dyed_brush")).isPresent(), "brush recipe by id");
        try {
            ModConfigValues.dyedBrush.set(false);
            h.assertTrue(craftRing(h, ItemRegistry.AMBER_DYE, Items.BRUSH).isEmpty(), "option off: brush still crafts");
            h.assertTrue(h.getLevel().getRecipeManager().byKey(NekomasFixed.id("amber_dyed_brush")).isEmpty(), "option off: recipe by id");
            h.assertTrue(h.getLevel().getRecipeManager().getRecipes().stream().noneMatch(r -> r.id().equals(NekomasFixed.id("amber_dyed_brush"))), "option off: recipe list");
            ItemStack slabs = craft(h, 3, 1, s(Items.RED_WOOL), s(Items.RED_WOOL), s(Items.RED_WOOL));
            h.assertTrue(!slabs.isEmpty(), "option off leaves other recipes alone");
        } finally {
            ModConfigValues.dyedBrush.set(true);
        }
        h.assertTrue(craftRing(h, ItemRegistry.AMBER_DYE, Items.BRUSH).is(amberBrush), "option back on");
        h.succeed();
    }

    private static void brush(GameTestHelper h, AllDyes dye, BlockPos relative, ItemStack stack) {
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(h, relative, Direction.UP)));
    }

    @GameTest(template = "platform")
    public static void brushRecolours(GameTestHelper h) {
        ItemStack maroon = new ItemStack(ItemDyeMap.BRUSH.get(AllDyes.MAROON));
        h.setBlock(AT, Blocks.WHITE_WOOL);
        brush(h, AllDyes.MAROON, AT, maroon);
        h.assertBlockPresent(BlockDyeMap.WOOL.get(AllDyes.MAROON), AT);
        h.assertTrue(maroon.getDamageValue() == 1, "brush damage " + maroon.getDamageValue());

        // Slabs and stairs keep their shape.
        ItemStack red = new ItemStack(ItemDyeMap.BRUSH.get(AllDyes.RED));
        h.setBlock(AT, BlockDyeMap.WOOL_SLABS.get(AllDyes.AMBER).defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        brush(h, AllDyes.RED, AT, red);
        h.assertBlockState(AT, s -> s.is(BlockDyeMap.WOOL_SLABS.get(AllDyes.RED)) && s.getValue(SlabBlock.TYPE) == SlabType.TOP, () -> "red wool slab on top");
        h.setBlock(AT, BlockDyeMap.SPOTTED_WOOL_STAIRS.get(AllDyes.WHITE).defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
        brush(h, AllDyes.RED, AT, red);
        h.assertBlockState(AT, s -> s.is(BlockDyeMap.SPOTTED_WOOL_STAIRS.get(AllDyes.RED)) && s.getValue(StairBlock.FACING) == Direction.EAST, () -> "red spotted stairs facing east");
        h.setBlock(AT, BlockDyeMap.CONCRETE_STAIRS.get(AllDyes.INDIGO).defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
        brush(h, AllDyes.RED, AT, red);
        h.assertBlockState(AT, s -> s.is(BlockDyeMap.CONCRETE_STAIRS.get(AllDyes.RED)) && s.getValue(StairBlock.FACING) == Direction.WEST, () -> "red concrete stairs facing west");

        // Terracotta, glass and bricks.
        h.setBlock(AT, Blocks.TERRACOTTA);
        brush(h, AllDyes.RED, AT, red);
        h.assertBlockPresent(Blocks.RED_TERRACOTTA, AT);
        h.setBlock(AT, Blocks.GLASS);
        brush(h, AllDyes.RED, AT, red);
        h.assertBlockPresent(Blocks.RED_STAINED_GLASS, AT);
        h.setBlock(AT, Blocks.BRICKS);
        brush(h, AllDyes.RED, AT, red);
        h.assertBlockPresent(BlockDyeMap.BRICKS.get(AllDyes.RED), AT);

        // Same colour, or a block it can't dye: the brush keeps its durability.
        int damage = red.getDamageValue();
        h.setBlock(AT, Blocks.RED_WOOL);
        brush(h, AllDyes.RED, AT, red);
        h.setBlock(AT, Blocks.STONE);
        brush(h, AllDyes.RED, AT, red);
        h.assertBlockPresent(Blocks.STONE, AT);
        h.assertTrue(red.getDamageValue() == damage, "brush used on nothing");

        // A shulker box keeps what is inside.
        h.setBlock(AT, Blocks.SHULKER_BOX);
        ShulkerBoxBlockEntity box = h.getBlockEntity(AT);
        box.setItem(3, new ItemStack(Items.DIAMOND, 7));
        brush(h, AllDyes.RED, AT, red);
        h.assertBlockPresent(Blocks.RED_SHULKER_BOX, AT);
        ShulkerBoxBlockEntity newBox = h.getBlockEntity(AT);
        h.assertTrue(newBox.getItem(3).is(Items.DIAMOND) && newBox.getItem(3).getCount() == 7, "shulker contents");

        // A bed changes both halves.
        BlockState foot = Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST).setValue(BedBlock.PART, BedPart.FOOT);
        BlockPos bedAt = new BlockPos(0, 2, 2);
        h.setBlock(bedAt.east(), foot.setValue(BedBlock.PART, BedPart.HEAD));
        h.setBlock(bedAt, foot);
        brush(h, AllDyes.RED, bedAt, red);
        h.assertBlockPresent(Blocks.RED_BED, bedAt);
        h.assertBlockPresent(Blocks.RED_BED, bedAt.east());
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 60)
    public static void brushInADispenser(GameTestHelper h) {
        BlockPos dispenserAt = new BlockPos(0, 2, 1);
        h.setBlock(dispenserAt, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
        h.setBlock(AT, Blocks.WHITE_CONCRETE);
        DispenserBlockEntity dispenser = h.getBlockEntity(dispenserAt);
        dispenser.setItem(0, new ItemStack(ItemDyeMap.BRUSH.get(AllDyes.AQUA)));
        h.setBlock(dispenserAt.above(), Blocks.REDSTONE_BLOCK);
        h.succeedWhen(() -> {
            h.assertBlockPresent(BlockDyeMap.CONCRETE.get(AllDyes.AQUA), AT);
            h.assertTrue(dispenser.getItem(0).is(ItemDyeMap.BRUSH.get(AllDyes.AQUA)) && dispenser.getItem(0).getDamageValue() == 1, "dispensed brush " + dispenser.getItem(0));
        });
    }

    @GameTest(template = "platform")
    public static void fireworksTakeTheExtraDyes(GameTestHelper h) {
        ItemStack star = craft(h, 3, 1, s(Items.GUNPOWDER), s(Items.RED_DYE), s(ItemRegistry.AMBER_DYE));
        h.assertTrue(star.is(Items.FIREWORK_STAR), "star recipe " + star);
        IntList colours = star.get(DataComponents.FIREWORK_EXPLOSION).colors();
        h.assertTrue(colours.size() == 2 && colours.getInt(1) == ModColors.AMBER.getColor(), "star colours " + colours);

        ItemStack faded = craft(h, 2, 1, star, s(ItemRegistry.AQUA_DYE));
        FireworkExplosion fadedExplosion = faded.get(DataComponents.FIREWORK_EXPLOSION);
        h.assertTrue(faded.is(Items.FIREWORK_STAR) && fadedExplosion.fadeColors().getInt(0) == ModColors.AQUA.getColor() && fadedExplosion.colors().equals(colours), "faded star " + fadedExplosion);

        ItemStack plain = craft(h, 2, 1, s(Items.GUNPOWDER), s(Items.BLUE_DYE));
        h.assertTrue(plain.is(Items.FIREWORK_STAR) && plain.get(DataComponents.FIREWORK_EXPLOSION).colors().size() == 1, "vanilla star");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void randomLootFunctionsAreRegistered(GameTestHelper h) {
        h.assertTrue(BuiltInRegistries.LOOT_FUNCTION_TYPE.get(NekomasFixed.id("random_dye")) == BrushRegistry.RANDOM_DYE, "random_dye");
        h.assertTrue(BuiltInRegistries.LOOT_FUNCTION_TYPE.get(NekomasFixed.id("random_firework")) == BrushRegistry.RANDOM_FIREWORK, "random_firework");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void bedsGetMessyAtNight(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        long time = level.getDayTime();
        BlockPos footPos = new BlockPos(0, 2, 2);
        BlockPos headPos = footPos.east();
        BlockState foot = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST).setValue(BedBlock.PART, BedPart.FOOT);
        h.setBlock(headPos, foot.setValue(BedBlock.PART, BedPart.HEAD));
        h.setBlock(footPos, foot);
        h.assertTrue(!Blocks.RED_BED.defaultBlockState().getValue(MessyBedAccessor.MESSY), "a new bed is made");
        h.assertTrue(PoiTypes.forState(h.getBlockState(headPos)).isPresent(), "a made bed is a home");

        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        try {
            // Day: it stays made.
            level.setDayTime(1000);
            level.updateSkyBrightness();
            h.getBlockState(footPos).useWithoutItem(level, player, hit(h, footPos, Direction.UP));
            h.assertTrue(!h.getBlockState(footPos).getValue(MessyBedAccessor.MESSY), "messy during the day");

            // Night, option off: it stays made.
            level.setDayTime(18000);
            level.updateSkyBrightness();
            ModConfigValues.enableMessyBeds.set(false);
            h.getBlockState(footPos).useWithoutItem(level, player, hit(h, footPos, Direction.UP));
            h.assertTrue(!h.getBlockState(footPos).getValue(MessyBedAccessor.MESSY), "messy with the option off");
            player.stopSleeping();
            h.setBlock(headPos, foot.setValue(BedBlock.PART, BedPart.HEAD));
            h.setBlock(footPos, foot);

            // Night: both halves get messy.
            ModConfigValues.enableMessyBeds.set(true);
            h.getBlockState(footPos).useWithoutItem(level, player, hit(h, footPos, Direction.UP));
            h.assertTrue(h.getBlockState(footPos).getValue(MessyBedAccessor.MESSY) && h.getBlockState(headPos).getValue(MessyBedAccessor.MESSY), "messy at night");
            h.assertTrue(PoiTypes.forState(h.getBlockState(headPos)).isEmpty(), "villagers leave a messy bed alone");
            player.stopSleeping();

            // Crouching with an empty hand makes it again.
            h.setBlock(headPos, h.getBlockState(headPos).setValue(BedBlock.OCCUPIED, false));
            h.setBlock(footPos, h.getBlockState(footPos).setValue(BedBlock.OCCUPIED, false));
            player.setShiftKeyDown(true);
            h.getBlockState(footPos).useWithoutItem(level, player, hit(h, footPos, Direction.UP));
            h.assertTrue(!h.getBlockState(footPos).getValue(MessyBedAccessor.MESSY) && !h.getBlockState(headPos).getValue(MessyBedAccessor.MESSY), "made again");
        } finally {
            ModConfigValues.enableMessyBeds.set(true);
            level.setDayTime(time);
            level.updateSkyBrightness();
        }
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void extraDyesWriteOnSigns(GameTestHelper h) {
        h.setBlock(AT, Blocks.OAK_SIGN);
        var sign = (net.minecraft.world.level.block.entity.SignBlockEntity) h.getBlockEntity(AT);
        sign.setText(sign.getFrontText().setMessage(0, net.minecraft.network.chat.Component.literal("hi")), true);
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        boolean applied = ((net.minecraft.world.item.SignApplicator) ItemRegistry.MAROON_DYE).tryApplyToSign(h.getLevel(), sign, true, player);
        h.assertTrue(applied, "maroon dye on a sign");
        var colour = sign.getFrontText().getMessage(0, false).getStyle().getColor();
        h.assertTrue(colour != null && colour.getValue() == (ModColors.MAROON.getColor() & 0xFFFFFF), "sign colour " + colour);
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void newAdvancementsLoad(GameTestHelper h) {
        var advancements = h.getLevel().getServer().getAdvancements();
        for (String name : List.of("mod/root", "mod/rainbow_spotted_wool", "mod/hollow_knight", "mod/slice_and_dice")) {
            h.assertTrue(advancements.get(NekomasFixed.id(name)) != null, name);
        }
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void potLootKeepsComponents(GameTestHelper h) {
        NonNullList<ItemStack> unused = NonNullList.create();
        TerracottaDecoratedPotBlockEntity pot = placePot(h, new PotDecorations(Items.BLADE_POTTERY_SHERD, Items.BRICK, Items.BRICK, Items.BRICK));
        pot.faceDecoration = new PotFaceDecoration(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.defaultBlockState());
        ServerLevel level = h.getLevel();
        BlockState state = pot.getBlockState();
        List<ItemStack> whole = Block.getDrops(state, level, pot.getBlockPos(), pot);
        h.assertTrue(whole.size() == 1 && whole.getFirst().is(PotRegistry.TERRACOTTA_DECORATED_POT_ITEM), "drops " + whole);
        h.assertTrue(whole.getFirst().get(DataComponents.POT_DECORATIONS).back().isPresent(), "sherds kept " + whole);
        h.assertTrue(whole.getFirst().get(PotRegistry.POT_FACE_BLOCK).getSafeBlock() == Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, "face kept " + whole);
        List<ItemStack> cracked = Block.getDrops(state.setValue(net.minecraft.world.level.block.DecoratedPotBlock.CRACKED, true), level, pot.getBlockPos(), pot);
        h.assertTrue(cracked.size() == 4 && cracked.stream().anyMatch(i -> i.is(Items.BLADE_POTTERY_SHERD)) && cracked.stream().anyMatch(i -> i.is(Items.BRICK)), "shards " + cracked);
        h.succeed();
    }
}
