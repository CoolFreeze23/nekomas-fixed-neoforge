package net.greenjab.nekomasfixed.gametest;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.registries.BlockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(NekomasFixed.NAMESPACE)
@PrefixGameTestTemplate(false)
public class ParityGameTests {
    private static final List<String> VILLAGES = List.of("plains", "desert", "savanna", "snowy", "taiga");

    @GameTest(template = "platform")
    public static void pyrotechnistHousesJoinVillagePools(GameTestHelper h) {
        var pools = h.getLevel().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        for (String village : VILLAGES) {
            var pool = pools.get(ResourceLocation.withDefaultNamespace("village/" + village + "/houses"));
            h.assertTrue(pool != null, "no pool for " + village);
            String wanted = NekomasFixed.id("village/" + village + "/pyrotechnist_house_" + village).toString();
            long count = pool.templates.stream().map(StructurePoolElement::toString).filter(text -> text.contains(wanted)).count();
            h.assertTrue(count == 2, village + " should list the house with weight 2, found " + count);
        }
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void pyrotechnistHousesLoad(GameTestHelper h) {
        var manager = h.getLevel().getStructureManager();
        for (String village : VILLAGES) {
            var template = manager.get(NekomasFixed.id("village/" + village + "/pyrotechnist_house_" + village));
            h.assertTrue(template.isPresent(), "missing structure for " + village);
            h.assertTrue(template.get().getSize().getX() > 0, "empty structure for " + village);
            var blocks = template.get().filterBlocks(net.minecraft.core.BlockPos.ZERO,
                    new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings(), BlockRegistry.PYROTECHNICS_TABLE);
            h.assertTrue(!blocks.isEmpty(), village + " house should hold a pyrotechnics table");
        }
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void villageLootTablesExist(GameTestHelper h) {
        var loot = h.getLevel().getServer().reloadableRegistries();
        for (String name : List.of("chests/village/pyrotechnist_loot", "chests/village/pyrotechnist_dispenser")) {
            LootTable table = loot.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, NekomasFixed.id(name)));
            h.assertTrue(table != LootTable.EMPTY, "missing loot table " + name);
        }
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void vanillaTagsTakeModContent(GameTestHelper h) {
        h.assertTrue(BlockRegistry.BAOBAB_FENCE_GATE.defaultBlockState().is(BlockTags.FENCE_GATES), "fence gates");
        h.assertTrue(BlockRegistry.AMBER_STAINED_GLASS.defaultBlockState().is(BlockTags.IMPERMEABLE), "impermeable glass");
        h.assertTrue(Blocks.MAGMA_BLOCK != null, "magma");
        h.assertTrue(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(NekomasFixed.id("goat_horn_iron_helmet")))
                .is(net.minecraft.tags.ItemTags.TRIMMABLE_ARMOR), "goat horn helmets take trims");
        h.assertTrue(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(NekomasFixed.id("baobab_boat")))
                .is(net.minecraft.tags.ItemTags.BOATS), "boats tag");
        h.assertTrue(!Items.AIR.getDefaultInstance().is(net.minecraft.tags.ItemTags.BOATS), "air is no boat");
        h.succeed();
    }
}
