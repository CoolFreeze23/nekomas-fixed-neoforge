package net.greenjab.nekomasfixed.gametest;

import com.mojang.serialization.JsonOps;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.FletchingTableBlock;
import net.greenjab.nekomasfixed.registry.block.entity.KilnBlockEntity;
import net.greenjab.nekomasfixed.registry.entity.CustomArrow;
import net.greenjab.nekomasfixed.registry.item.PrismItem;
import net.greenjab.nekomasfixed.registry.item.quiver.QuiverContents;
import net.greenjab.nekomasfixed.registry.other.ArmorGlint;
import net.greenjab.nekomasfixed.registry.other.GlintColor;
import net.greenjab.nekomasfixed.registry.other.TippedArrowCustomComponent;
import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.greenjab.nekomasfixed.registry.registries.DataComponentPredicateRegistry;
import net.greenjab.nekomasfixed.registry.registries.GlintColorRegistry;
import net.greenjab.nekomasfixed.registry.registries.GlintRegistry;
import net.greenjab.nekomasfixed.registry.registries.KilnRegistry;
import net.greenjab.nekomasfixed.screen.FletchingMenu;
import net.greenjab.nekomasfixed.screen.KilnMenu;
import net.greenjab.nekomasfixed.util.AllDyes;
import net.greenjab.nekomasfixed.util.LoadedCrossbowModifier;
import net.greenjab.nekomasfixed.util.ModRecipeBookType;
import net.greenjab.nekomasfixed.util.TreasureGlints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@GameTestHolder(NekomasFixed.NAMESPACE)
@PrefixGameTestTemplate(false)
public class KilnQuiverGlintGameTests {
    private static final BlockPos AT = new BlockPos(1, 2, 1);

    private static ItemStack potion(Holder<net.minecraft.world.item.alchemy.Potion> type) {
        return PotionContents.createItemStack(Items.POTION, type);
    }

    private static ItemStack quiverWith(ItemStack... stacks) {
        ItemStack quiver = new ItemStack(ArcheryRegistry.QUIVER);
        QuiverContents.Mutable contents = new QuiverContents.Mutable(QuiverContents.EMPTY);
        for (ItemStack stack : stacks) {
            contents.tryInsert(stack);
        }
        quiver.set(ArcheryRegistry.QUIVER_CONTENTS, contents.toImmutable());
        return quiver;
    }

    private static ItemStack tippedArrows(int count) {
        ItemStack stack = new ItemStack(ArcheryRegistry.TIPPED_ARROW_CUSTOM, count);
        stack.set(ArcheryRegistry.TIPPED_POTION_CONTENTS, new TippedArrowCustomComponent(List.of(new PotionContents(Potions.SWIFTNESS), new PotionContents(Potions.POISON))));
        return stack;
    }

    private static ServerPlayer survivalPlayer(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        return player;
    }

    // ---- kiln ----

    @GameTest(template = "platform")
    public static void kilnRecipesLoad(GameTestHelper h) {
        var manager = h.getLevel().getRecipeManager();
        record Case(net.minecraft.world.item.Item in, net.minecraft.world.item.Item out) {}
        List<Case> cases = List.of(
                new Case(Items.SAND, Items.GLASS),
                new Case(Items.COBBLESTONE, Items.STONE),
                new Case(Items.CLAY_BALL, Items.BRICK),
                new Case(Items.CHORUS_FRUIT, Items.POPPED_CHORUS_FRUIT),
                new Case(Items.RED_TERRACOTTA, Items.RED_GLAZED_TERRACOTTA));
        for (Case c : cases) {
            var recipe = manager.getRecipeFor(KilnRegistry.KILN_RECIPE, new SingleRecipeInput(new ItemStack(c.in())), h.getLevel());
            h.assertTrue(recipe.isPresent(), "no kiln recipe for " + c.in());
            h.assertTrue(recipe.get().value().assemble(new SingleRecipeInput(new ItemStack(c.in())), h.getLevel().registryAccess()).is(c.out()), "wrong kiln result for " + c.in());
        }
        h.assertTrue(manager.getRecipeFor(KilnRegistry.KILN_RECIPE, new SingleRecipeInput(new ItemStack(Items.IRON_ORE)), h.getLevel()).isEmpty(), "the kiln should not smelt iron ore");
        h.assertTrue(manager.getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(new ItemStack(Items.SAND)), h.getLevel()).isEmpty(), "sand should not blast");
        h.assertTrue(manager.byKey(NekomasFixed.id("resin_brick_from_kilning")).isEmpty(), "no resin recipe on 1.21.1");
        h.assertTrue(manager.byKey(NekomasFixed.id("kiln")).isPresent(), "the kiln's crafting recipe");
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 300)
    public static void kilnSmeltsAndBurnsFuelTwiceAsFast(GameTestHelper h) {
        h.setBlock(AT, KilnRegistry.KILN);
        KilnBlockEntity kiln = (KilnBlockEntity) h.getBlockEntity(AT);
        kiln.setItem(0, new ItemStack(Items.SAND, 2));
        kiln.setItem(1, new ItemStack(Items.COAL));
        h.runAfterDelay(5, () -> {
            CompoundTag tag = kiln.saveWithoutMetadata(h.getLevel().registryAccess());
            int burn = tag.getShort("BurnTime");
            h.assertTrue(burn > 790 && burn <= 800, "coal should burn 800 ticks in a kiln, got " + burn);
            h.assertTrue(h.getBlockState(AT).getValue(AbstractFurnaceBlock.LIT), "the kiln should be lit");
        });
        // 100 ticks a piece.
        h.runAfterDelay(106, () -> {
            h.assertTrue(kiln.getItem(2).is(Items.GLASS), "the kiln should have made glass, got " + kiln.getItem(2));
            h.assertTrue(kiln.getItem(0).getCount() == 1, "one sand should be used");
            h.succeed();
        });
    }

    @GameTest(template = "platform", timeoutTicks = 120)
    public static void kilnIgnoresOtherRecipes(GameTestHelper h) {
        h.setBlock(AT, KilnRegistry.KILN);
        KilnBlockEntity kiln = (KilnBlockEntity) h.getBlockEntity(AT);
        kiln.setItem(0, new ItemStack(Items.IRON_ORE));
        kiln.setItem(1, new ItemStack(Items.COAL));
        h.runAfterDelay(20, () -> {
            h.assertTrue(kiln.getItem(1).getCount() == 1, "the kiln should not burn fuel for iron ore");
            h.assertTrue(kiln.getItem(2).isEmpty(), "no output");
            h.succeed();
        });
    }

    @GameTest(template = "platform")
    public static void kilnOpensItsMenu(GameTestHelper h) {
        h.setBlock(AT, KilnRegistry.KILN);
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        BlockPos abs = h.absolutePos(AT);
        h.getLevel().getBlockState(abs).useWithoutItem(h.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
        h.assertTrue(player.containerMenu instanceof KilnMenu, "the kiln should open a KilnMenu, got " + player.containerMenu);
        h.assertTrue(((KilnMenu) player.containerMenu).getRecipeBookType() == ModRecipeBookType.KILN, "the kiln's own recipe book");
        h.assertTrue(h.getLevel().getBlockState(abs).is(BlockTags.MINEABLE_WITH_PICKAXE), "the kiln should be mined with a pickaxe");
        h.succeed();
    }

    // ---- fletching ----

    @GameTest(template = "platform")
    public static void fletchingTableOpensItsMenuAndGivesItemsBack(GameTestHelper h) {
        h.assertTrue(Blocks.FLETCHING_TABLE instanceof FletchingTableBlock, "minecraft:fletching_table should be the mod's block");
        h.assertTrue(BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace("fletching_table")) == Blocks.FLETCHING_TABLE, "fletching table id");
        h.setBlock(AT, Blocks.FLETCHING_TABLE);
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        BlockPos abs = h.absolutePos(AT);
        h.getLevel().getBlockState(abs).useWithoutItem(h.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
        h.assertTrue(player.containerMenu instanceof FletchingMenu, "the table should open a FletchingMenu, got " + player.containerMenu);
        FletchingMenu menu = (FletchingMenu) player.containerMenu;
        h.assertTrue(menu.slots.size() == 46, "3x3 grid, result and inventory");
        menu.getSlot(1).set(new ItemStack(Items.FLINT, 5));
        player.closeContainer();
        h.assertTrue(player.getInventory().countItem(Items.FLINT) == 5, "closing should hand the grid back");
        h.succeed();
    }

    // ---- tipped arrows ----

    private static RecipeHolder<?> tippedRecipe(GameTestHelper h) {
        Optional<RecipeHolder<?>> recipe = h.getLevel().getRecipeManager().byKey(NekomasFixed.id("tipped_custom_arrow"));
        h.assertTrue(recipe.isPresent(), "tipped_custom_arrow should load");
        return recipe.get();
    }

    @SuppressWarnings("unchecked")
    @GameTest(template = "platform")
    public static void tippedArrowRecipe(GameTestHelper h) {
        Recipe<CraftingInput> recipe = (Recipe<CraftingInput>) tippedRecipe(h).value();
        h.assertTrue(recipe instanceof CraftingRecipe, "a crafting recipe");

        CraftingInput good = CraftingInput.of(3, 1, List.of(new ItemStack(Items.ARROW, 16), potion(Potions.SWIFTNESS), potion(Potions.POISON)));
        h.assertTrue(recipe.matches(good, h.getLevel()), "arrows and two different potions");
        ItemStack result = recipe.assemble(good, h.getLevel().registryAccess());
        h.assertTrue(result.is(ArcheryRegistry.TIPPED_ARROW_CUSTOM) && result.getCount() == 16, "the whole stack is tipped, got " + result);
        h.assertTrue(result.get(ArcheryRegistry.TIPPED_POTION_CONTENTS).potionContents().size() == 2, "both potions kept");
        h.assertTrue(result.is(ItemTags.ARROWS), "#minecraft:arrows");

        h.assertFalse(recipe.matches(CraftingInput.of(2, 1, List.of(new ItemStack(Items.ARROW), new ItemStack(Items.ARROW))), h.getLevel()), "no potion");
        h.assertFalse(recipe.matches(CraftingInput.of(1, 1, List.of(potion(Potions.SWIFTNESS))), h.getLevel()), "no arrow");
        h.assertFalse(recipe.matches(CraftingInput.of(3, 1, List.of(new ItemStack(Items.ARROW), potion(Potions.SWIFTNESS), potion(Potions.SWIFTNESS))), h.getLevel()), "the same potion twice");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void takingTippedArrowsLeavesBottles(GameTestHelper h) {
        h.setBlock(AT, Blocks.CRAFTING_TABLE);
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();
        CraftingMenu menu = new CraftingMenu(1, player.getInventory(), ContainerLevelAccess.create(h.getLevel(), h.absolutePos(AT)));
        menu.getSlot(1).set(new ItemStack(Items.ARROW, 16));
        menu.getSlot(2).set(potion(Potions.SWIFTNESS));
        h.assertTrue(menu.getSlot(0).getItem().is(ArcheryRegistry.TIPPED_ARROW_CUSTOM), "the grid should show the result, got " + menu.getSlot(0).getItem());

        menu.clicked(0, 0, ClickType.PICKUP, player);
        h.assertTrue(player.getInventory().countItem(ArcheryRegistry.TIPPED_ARROW_CUSTOM) == 16, "16 tipped arrows should reach the inventory");
        h.assertTrue(menu.getSlot(1).getItem().isEmpty(), "the arrows are used up");
        h.assertTrue(menu.getSlot(2).getItem().is(Items.GLASS_BOTTLE), "the potion leaves a bottle");
        h.assertTrue(menu.getCarried().isEmpty(), "nothing on the cursor");
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 100)
    public static void tippedArrowLeavesACloudPerPotion(GameTestHelper h) {
        Vec3 pos = h.absoluteVec(new Vec3(1.5, 3.5, 1.5));
        Arrow arrow = new Arrow(h.getLevel(), pos.x, pos.y, pos.z, tippedArrows(1), null);
        arrow.setDeltaMovement(0, -1, 0);
        h.getLevel().addFreshEntity(arrow);
        h.succeedWhen(() -> {
            int clouds = h.getLevel().getEntitiesOfClass(AreaEffectCloud.class, new net.minecraft.world.phys.AABB(h.absolutePos(AT)).inflate(3)).size();
            h.assertTrue(clouds == 2, "expected a cloud per potion, got " + clouds);
        });
    }

    @GameTest(template = "platform")
    public static void dispensedArrowIsACustomArrowAtTheDispenser(GameTestHelper h) {
        Vec3 pos = h.absoluteVec(new Vec3(1.5, 3.5, 1.5));
        var item = (net.minecraft.world.item.ProjectileItem) ArcheryRegistry.TIPPED_ARROW_CUSTOM;
        var projectile = item.asProjectile(h.getLevel(), pos, tippedArrows(3), Direction.UP);
        h.assertTrue(projectile instanceof CustomArrow, "a CustomArrow");
        h.assertTrue(projectile.position().distanceTo(pos) < 0.01, "at the dispenser, got " + projectile.position());
        ItemStack origin = ((AbstractArrow) projectile).getPickupItemStackOrigin();
        h.assertTrue(origin.getCount() == 1 && origin.has(ArcheryRegistry.TIPPED_POTION_CONTENTS), "keeps one of the arrows");
        h.succeed();
    }

    // ---- quiver ----

    @GameTest(template = "platform")
    public static void quiverInsertAndExtract(GameTestHelper h) {
        ServerPlayer player = survivalPlayer(h);
        ItemStack quiver = new ItemStack(ArcheryRegistry.QUIVER);

        // Clicking a quiver with arrows on the cursor.
        ItemStack carried = new ItemStack(Items.ARROW, 20);
        SimpleContainer box = new SimpleContainer(2);
        Slot slot = new Slot(box, 0, 0, 0);
        slot.set(quiver);
        SlotAccess cursor = SlotAccess.of(() -> ItemStack.EMPTY, stack -> {
        });
        h.assertTrue(quiver.overrideOtherStackedOnMe(carried, slot, ClickAction.PRIMARY, player, cursor), "arrows go in");
        h.assertTrue(carried.isEmpty(), "the cursor stack is used up");
        QuiverContents contents = quiver.get(ArcheryRegistry.QUIVER_CONTENTS);
        h.assertTrue(contents.getTotalArrowCount() == 20 && contents.size() == 1, "20 arrows in one stack");

        // Non arrows stay out.
        ItemStack dirt = new ItemStack(Items.DIRT, 4);
        quiver.overrideOtherStackedOnMe(dirt, slot, ClickAction.PRIMARY, player, cursor);
        h.assertTrue(dirt.getCount() == 4 && quiver.get(ArcheryRegistry.QUIVER_CONTENTS).getTotalArrowCount() == 20, "dirt stays out");

        // Clicking a slot of arrows with the quiver.
        Slot arrows = new Slot(box, 1, 0, 0);
        arrows.set(new ItemStack(Items.SPECTRAL_ARROW, 8));
        h.assertTrue(quiver.overrideStackedOnOther(arrows, ClickAction.PRIMARY, player), "the quiver takes the slot");
        h.assertTrue(arrows.getItem().isEmpty(), "the slot is empty");
        h.assertTrue(quiver.get(ArcheryRegistry.QUIVER_CONTENTS).items().getFirst().is(Items.SPECTRAL_ARROW), "the newest stack is first");

        // Right click on an empty slot takes the first stack out.
        h.assertTrue(quiver.overrideStackedOnOther(arrows, ClickAction.SECONDARY, player), "the quiver puts a stack down");
        h.assertTrue(arrows.getItem().is(Items.SPECTRAL_ARROW) && arrows.getItem().getCount() == 8, "got " + arrows.getItem());
        h.assertTrue(quiver.get(ArcheryRegistry.QUIVER_CONTENTS).getTotalArrowCount() == 20, "20 left");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void quiverHoldsNineStacksAndGivesTheRestBack(GameTestHelper h) {
        ServerPlayer player = survivalPlayer(h);
        ItemStack quiver = new ItemStack(ArcheryRegistry.QUIVER);
        QuiverContents.Mutable contents = new QuiverContents.Mutable(QuiverContents.EMPTY);
        for (int i = 0; i < 8; i++) {
            contents.items.add(new ItemStack(Items.ARROW, 64));
        }
        contents.items.add(new ItemStack(Items.SPECTRAL_ARROW, 60));
        quiver.set(ArcheryRegistry.QUIVER_CONTENTS, contents.toImmutable());

        SimpleContainer box = new SimpleContainer(1);
        Slot slot = new Slot(box, 0, 0, 0);
        slot.set(new ItemStack(Items.SPECTRAL_ARROW, 10));
        quiver.overrideStackedOnOther(slot, ClickAction.PRIMARY, player);
        QuiverContents after = quiver.get(ArcheryRegistry.QUIVER_CONTENTS);
        h.assertTrue(after.getTotalArrowCount() == QuiverContents.MAX_ARROWS, "full at 576, got " + after.getTotalArrowCount());
        h.assertTrue(slot.getItem().getCount() == 6, "6 arrows should stay in the slot, got " + slot.getItem());
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void bowDrawsFromTheQuiverFirst(GameTestHelper h) {
        ServerPlayer player = survivalPlayer(h);
        player.getInventory().clearContent();
        ItemStack bow = new ItemStack(Items.BOW);
        player.setItemInHand(InteractionHand.MAIN_HAND, bow);
        player.getInventory().setItem(9, new ItemStack(Items.ARROW, 10));
        ItemStack quiver = quiverWith(new ItemStack(Items.ARROW, 5), tippedArrows(2));
        player.getInventory().setItem(10, quiver);

        ItemStack ammo = player.getProjectile(bow);
        h.assertTrue(ammo.is(ArcheryRegistry.TIPPED_ARROW_CUSTOM), "the quiver's first stack comes first, got " + ammo);

        ((BowItem) Items.BOW).releaseUsing(bow, h.getLevel(), player, Items.BOW.getUseDuration(bow, player) - 30);
        QuiverContents contents = quiver.get(ArcheryRegistry.QUIVER_CONTENTS);
        h.assertTrue(contents.getTotalArrowCount() == 6, "one arrow leaves the quiver, got " + contents.getTotalArrowCount());
        h.assertTrue(player.getInventory().getItem(9).getCount() == 10, "loose arrows are left alone");

        var fired = h.getLevel().getEntitiesOfClass(Arrow.class, player.getBoundingBox().inflate(8), arrow -> arrow.getOwner() == player);
        h.assertTrue(fired.size() == 1, "one arrow fired, got " + fired.size());
        h.assertTrue(fired.getFirst().getPickupItemStackOrigin().has(ArcheryRegistry.TIPPED_POTION_CONTENTS), "the fired arrow keeps its potions");

        // Once the quiver is empty, the inventory is used again.
        quiver.set(ArcheryRegistry.QUIVER_CONTENTS, QuiverContents.EMPTY);
        h.assertTrue(player.getProjectile(bow) == player.getInventory().getItem(9), "the loose stack after the quiver");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void crossbowLoadsFromTheQuiverAndArrowsInHandComeFirst(GameTestHelper h) {
        ServerPlayer player = survivalPlayer(h);
        player.getInventory().clearContent();
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        player.setItemInHand(InteractionHand.MAIN_HAND, crossbow);
        ItemStack quiver = quiverWith(new ItemStack(Items.SPECTRAL_ARROW, 3));
        player.getInventory().setItem(10, quiver);
        h.assertTrue(player.getProjectile(crossbow).is(Items.SPECTRAL_ARROW), "the quiver is found");

        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 1));
        h.assertTrue(player.getProjectile(crossbow).is(Items.ARROW), "held arrows come first");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void creativeBowDoesNotEmptyTheQuiver(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = true;
        player.getInventory().clearContent();
        ItemStack bow = new ItemStack(Items.BOW);
        player.setItemInHand(InteractionHand.MAIN_HAND, bow);
        ItemStack quiver = quiverWith(new ItemStack(Items.ARROW, 5));
        player.getInventory().setItem(10, quiver);
        ((BowItem) Items.BOW).releaseUsing(bow, h.getLevel(), player, Items.BOW.getUseDuration(bow, player) - 30);
        h.assertTrue(quiver.get(ArcheryRegistry.QUIVER_CONTENTS).getTotalArrowCount() == 5, "creative players keep their arrows");
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 100)
    public static void holdingUseDropsTheQuiver(GameTestHelper h) {
        ServerPlayer player = survivalPlayer(h);
        ItemStack quiver = quiverWith(new ItemStack(Items.ARROW, 5), new ItemStack(Items.SPECTRAL_ARROW, 5));
        quiver.getItem().onUseTick(h.getLevel(), player, quiver, 200);
        h.assertTrue(quiver.get(ArcheryRegistry.QUIVER_CONTENTS).size() == 1, "the first use tick drops a stack");
        quiver.getItem().onUseTick(h.getLevel(), player, quiver, 195);
        h.assertTrue(quiver.get(ArcheryRegistry.QUIVER_CONTENTS).size() == 1, "nothing during the pause");
        quiver.getItem().onUseTick(h.getLevel(), player, quiver, 188);
        h.assertTrue(quiver.get(ArcheryRegistry.QUIVER_CONTENTS).isEmpty(), "then a stack every other tick");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void hasArrowsPredicate(GameTestHelper h) {
        h.assertTrue(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE.getKey(DataComponentPredicateRegistry.HAS_ARROWS).equals(ResourceLocation.withDefaultNamespace("has_arrows")), "minecraft:has_arrows");
        var predicate = new net.greenjab.nekomasfixed.registry.other.predicates.QuiverPredicate();
        h.assertFalse(predicate.matches(new ItemStack(ArcheryRegistry.QUIVER)), "an empty quiver");
        h.assertTrue(predicate.matches(quiverWith(new ItemStack(Items.ARROW))), "a quiver with arrows");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void loadedCrossbowLootFunction(GameTestHelper h) {
        h.assertTrue(BuiltInRegistries.LOOT_FUNCTION_TYPE.containsKey(NekomasFixed.id("loaded_crossbow")), "registered");
        LootContext context = new LootContext.Builder(new LootParams.Builder(h.getLevel()).create(LootContextParamSets.EMPTY)).create(Optional.empty());
        ItemStack crossbow = new LoadedCrossbowModifier().apply(new ItemStack(Items.CROSSBOW), context);
        ChargedProjectiles charged = crossbow.get(DataComponents.CHARGED_PROJECTILES);
        h.assertTrue(charged != null && charged.contains(Items.FIREWORK_ROCKET), "loaded with a rocket");
        var enchantments = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        boolean quickCharge = crossbow.getEnchantments().getLevel(enchantments.getOrThrow(Enchantments.QUICK_CHARGE)) == 2;
        boolean multishot = crossbow.getEnchantments().getLevel(enchantments.getOrThrow(Enchantments.MULTISHOT)) == 1;
        h.assertTrue(quickCharge || multishot, "enchanted");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void componentsSaveAndLoad(GameTestHelper h) {
        var registries = h.getLevel().registryAccess();
        ItemStack quiver = quiverWith(tippedArrows(3), new ItemStack(Items.ARROW, 7));
        Tag saved = quiver.save(registries);
        ItemStack loaded = ItemStack.parseOptional(registries, (CompoundTag) saved);
        h.assertTrue(ItemStack.matches(quiver, loaded), "quiver round trip: " + loaded);
        h.assertTrue(loaded.get(ArcheryRegistry.QUIVER_CONTENTS).items().stream().anyMatch(s -> s.has(ArcheryRegistry.TIPPED_POTION_CONTENTS)), "nested components survive");

        ItemStack arrows = tippedArrows(5);
        ItemStack arrowsLoaded = ItemStack.parseOptional(registries, (CompoundTag) arrows.save(registries));
        h.assertTrue(ItemStack.matches(arrows, arrowsLoaded), "tipped arrows round trip");

        var red = registries.lookupOrThrow(GlintColor.REGISTRY_KEY).getOrThrow(GlintColorRegistry.GLINT_COLORS.get(AllDyes.RED));
        ItemStack armor = new ItemStack(Items.IRON_CHESTPLATE);
        armor.set(GlintRegistry.ARMOR_GLINT, new ArmorGlint(red));
        ItemStack armorLoaded = ItemStack.parseOptional(registries, (CompoundTag) armor.save(registries));
        h.assertTrue(red.equals(armorLoaded.get(GlintRegistry.ARMOR_GLINT).color()), "glint round trip");

        var encoded = ArmorGlint.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), new ArmorGlint(red)).getOrThrow();
        h.assertTrue(encoded.getAsString().equals("nekomasfixed:red"), "a glint saves as its id, got " + encoded);
        h.succeed();
    }

    // ---- glints ----

    @GameTest(template = "platform")
    public static void everyPrismHasAGlint(GameTestHelper h) {
        var registries = h.getLevel().registryAccess();
        var glints = registries.lookupOrThrow(GlintColor.REGISTRY_KEY);
        h.assertTrue(glints.listElementIds().count() == 30, "30 glints, got " + glints.listElementIds().count());
        List<String> missing = new ArrayList<>();
        for (var dye : AllDyes.values()) {
            if (glints.get(GlintColorRegistry.GLINT_COLORS.get(dye)).isEmpty()) missing.add(dye.getSerializedName());
        }
        for (var glint : TreasureGlints.values()) {
            if (glints.get(GlintColorRegistry.TREASURE_GLINTS.get(glint)).isEmpty()) missing.add(glint.getSerializedName());
        }
        h.assertTrue(missing.isEmpty(), "no data for " + missing);
        var prisms = BuiltInRegistries.ITEM.getTag(net.minecraft.tags.TagKey.create(Registries.ITEM, NekomasFixed.id("prisms")));
        h.assertTrue(prisms.isPresent() && prisms.get().size() == 30, "30 prisms in the tag");
        for (var item : prisms.get()) {
            h.assertTrue(item.value() instanceof PrismItem, item.getRegisteredName() + " is a PrismItem");
        }
        GlintColor sculk = glints.getOrThrow(GlintColorRegistry.TREASURE_GLINTS.get(TreasureGlints.SCULK)).value();
        h.assertTrue(sculk.texture().isPresent(), "sculk has its own texture");
        GlintColor rainbow = glints.getOrThrow(GlintColorRegistry.TREASURE_GLINTS.get(TreasureGlints.RAINBOW)).value();
        h.assertTrue(rainbow.cycleTicks() == 200 && rainbow.colors().size() == 20, "rainbow cycle");
        h.assertTrue(rainbow.colorAt(0) != rainbow.colorAt(2500), "rainbow changes colour");
        h.assertTrue((rainbow.colorAt(0) >>> 24) == 255, "opaque");
        h.succeed();
    }

    @SuppressWarnings("unchecked")
    @GameTest(template = "platform")
    public static void glintRecipe(GameTestHelper h) {
        var registries = h.getLevel().registryAccess();
        Optional<RecipeHolder<?>> holder = h.getLevel().getRecipeManager().byKey(NekomasFixed.id("armor_glint"));
        h.assertTrue(holder.isPresent(), "armor_glint should load");
        SmithingRecipe recipe = (SmithingRecipe) holder.get().value();
        var glints = registries.lookupOrThrow(GlintColor.REGISTRY_KEY);
        var red = glints.getOrThrow(GlintColorRegistry.GLINT_COLORS.get(AllDyes.RED));
        var fire = glints.getOrThrow(GlintColorRegistry.TREASURE_GLINTS.get(TreasureGlints.FIRE));

        ItemStack armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
        SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(GlintRegistry.RED_PRISM), armor, new ItemStack(Items.LAPIS_LAZULI));
        h.assertTrue(recipe.matches(input, h.getLevel()), "prism, armor, lapis");
        ItemStack result = recipe.assemble(input, registries);
        h.assertTrue(result.is(Items.DIAMOND_CHESTPLATE) && red.equals(result.get(GlintRegistry.ARMOR_GLINT).color()), "red glint, got " + result);

        SmithingRecipeInput same = new SmithingRecipeInput(new ItemStack(GlintRegistry.RED_PRISM), result, new ItemStack(Items.LAPIS_LAZULI));
        h.assertTrue(recipe.assemble(same, registries).isEmpty(), "the same colour again is refused");

        SmithingRecipeInput other = new SmithingRecipeInput(new ItemStack(GlintRegistry.FIRE_PRISM), result, new ItemStack(Items.LAPIS_LAZULI));
        h.assertTrue(fire.equals(recipe.assemble(other, registries).get(GlintRegistry.ARMOR_GLINT).color()), "a treasure prism recolours");

        h.assertFalse(recipe.matches(new SmithingRecipeInput(new ItemStack(GlintRegistry.RED_PRISM), new ItemStack(Items.STICK), new ItemStack(Items.LAPIS_LAZULI)), h.getLevel()), "only armor");
        h.assertFalse(recipe.matches(new SmithingRecipeInput(new ItemStack(GlintRegistry.RED_PRISM), armor, new ItemStack(Items.DIAMOND)), h.getLevel()), "only lapis");

        // An explicit component on a stack also counts.
        ItemStack custom = new ItemStack(Items.STICK);
        custom.set(GlintRegistry.PROVIDES_GLINT, fire);
        h.assertTrue(recipe.assemble(new SmithingRecipeInput(custom, armor, new ItemStack(Items.LAPIS_LAZULI)), registries).get(GlintRegistry.ARMOR_GLINT).color().equals(fire), "provides_glint is honoured");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void advancementsAndRecipesLoad(GameTestHelper h) {
        var advancements = h.getLevel().getServer().getAdvancements();
        for (String id : List.of("mod/prismaniac", "mod/a_bow_but_worse", "recipes/misc/kiln", "recipes/building_blocks/glass_from_kilning")) {
            h.assertTrue(advancements.get(NekomasFixed.id(id)) != null, "advancement " + id);
        }
        h.assertTrue(h.getLevel().getRecipeManager().byKey(NekomasFixed.id("red_prism")).isPresent(), "prism crafting recipe");
        h.succeed();
    }
}
