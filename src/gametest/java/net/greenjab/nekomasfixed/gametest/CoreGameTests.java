package net.greenjab.nekomasfixed.gametest;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.MelonBlock;
import net.greenjab.nekomasfixed.registry.registries.BlockRegistry;
import net.greenjab.nekomasfixed.registry.registries.EntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(NekomasFixed.NAMESPACE)
@PrefixGameTestTemplate(false)
public class CoreGameTests {
    private static final String NS = NekomasFixed.NAMESPACE;

    @GameTest(template = "platform")
    public static void vanillaReplacementsAreInPlace(GameTestHelper h) {
        h.assertTrue(Blocks.MELON instanceof MelonBlock, "minecraft:melon should be a MelonBlock");
        h.assertTrue(Items.CLOCK instanceof BlockItem, "minecraft:clock should be a block item");
        h.assertTrue(BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("clock")) == Items.CLOCK, "clock id");
        h.assertTrue(BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace("melon")) == Blocks.MELON, "melon id");
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 400)
    public static void everyBlockPlaces(GameTestHelper h) {
        List<String> failed = new ArrayList<>();
        BlockPos at = new BlockPos(1, 2, 1);
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            if (!id.getNamespace().equals(NS)) continue;
            Block block = entry.getValue();
            try {
                BlockState state = block.defaultBlockState();
                h.setBlock(at, state);
                h.getLevel().getBlockEntity(h.absolutePos(at));
                state.getShape(h.getLevel(), h.absolutePos(at));
                h.setBlock(at, Blocks.AIR);
            } catch (Exception e) {
                failed.add(id + ": " + e);
            }
        }
        h.assertTrue(failed.isEmpty(), "blocks failed: " + failed);
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void everyEntitySpawns(GameTestHelper h) {
        List<String> failed = new ArrayList<>();
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            if (!id.getNamespace().equals(NS)) continue;
            EntityType<?> type = entry.getValue();
            try {
                Entity e = type.create(h.getLevel());
                if (e == null) {
                    failed.add(id + ": null");
                    continue;
                }
                if (e instanceof LivingEntity living) {
                    living.getMaxHealth();
                }
                e.moveTo(h.absoluteVec(new Vec3(1.5, 2, 1.5)));
                h.getLevel().addFreshEntity(e);
                e.tick();
                e.discard();
            } catch (Exception ex) {
                failed.add(id + ": " + ex);
            }
        }
        h.assertTrue(failed.isEmpty(), "entities failed: " + failed);
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void creativeTabsAreRegistered(GameTestHelper h) {
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(NekomasFixed.id("nekomasfixed"));
        h.assertTrue(tab != null, "main tab missing");
        h.assertTrue(BuiltInRegistries.CREATIVE_MODE_TAB.get(NekomasFixed.id("nekomasfixedcolours")) != null, "colours tab missing");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void biomeModifiersApply(GameTestHelper h) {
        var biomes = h.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> flower = biomes.getHolderOrThrow(Biomes.FLOWER_FOREST);
        boolean moobloom = flower.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap().stream()
                .anyMatch(s -> s.type == EntityTypeRegistry.MOOBLOOM);
        h.assertTrue(moobloom, "moobloom spawn missing from flower forest");
        boolean rime = biomes.getHolderOrThrow(Biomes.SNOWY_PLAINS).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                .anyMatch(s -> s.type == EntityTypeRegistry.RIME);
        h.assertTrue(rime, "rime spawn missing from snowy plains");
        var vegetal = biomes.getHolderOrThrow(Biomes.SAVANNA).value().getGenerationSettings().features()
                .get(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal());
        boolean baobab = vegetal.stream().anyMatch(f -> f.is(ResourceKey.create(Registries.PLACED_FEATURE, NekomasFixed.id("baobab"))));
        h.assertTrue(baobab, "baobab feature missing from savanna");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void baobabStrips(GameTestHelper h) {
        BlockPos at = new BlockPos(1, 2, 1);
        h.setBlock(at, BlockRegistry.BAOBAB_LOG);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_AXE.getDefaultInstance());
        BlockPos abs = h.absolutePos(at);
        var ctx = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
        BlockState stripped = h.getLevel().getBlockState(abs).getToolModifiedState(ctx, ItemAbilities.AXE_STRIP, false);
        h.assertTrue(stripped != null && stripped.is(BlockRegistry.STRIPPED_BAOBAB_LOG), "baobab log should strip, got " + stripped);
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void recipesAreLoaded(GameTestHelper h) {
        long ours = h.getLevel().getRecipeManager().getRecipes().stream().filter(r -> r.id().getNamespace().equals(NS)).count();
        h.assertTrue(ours > 100, "only " + ours + " recipes loaded");
        h.succeed();
    }
}
