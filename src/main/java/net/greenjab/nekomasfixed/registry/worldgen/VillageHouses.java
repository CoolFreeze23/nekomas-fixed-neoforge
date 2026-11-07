package net.greenjab.nekomasfixed.registry.worldgen;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Adds the pyrotechnist's house to each village type. Done here rather than by overriding vanilla's template pools
 * in data, so that other mods adding their own houses keep working.
 */
public class VillageHouses {
    private static final int WEIGHT = 2;
    private static final ResourceKey<StructureProcessorList> EMPTY =
            ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace("empty"));

    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        var pools = server.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        var processors = server.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST);
        add(pools, processors, "plains", ProcessorLists.MOSSIFY_10_PERCENT);
        add(pools, processors, "desert", EMPTY);
        add(pools, processors, "savanna", EMPTY);
        add(pools, processors, "snowy", EMPTY);
        add(pools, processors, "taiga", EMPTY);
    }

    private static void add(Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> processors, String village, ResourceKey<StructureProcessorList> processor) {
        var pool = pools.get(ResourceLocation.withDefaultNamespace("village/" + village + "/houses"));
        Holder<StructureProcessorList> list = processors.getHolder(processor).orElse(null);
        if (pool == null || list == null) return;
        String id = NekomasFixed.id("village/" + village + "/pyrotechnist_house_" + village).toString();
        StructurePoolElement element = StructurePoolElement.legacy(id, list).apply(StructureTemplatePool.Projection.RIGID);
        for (int i = 0; i < WEIGHT; i++) {
            pool.templates.add(element);
        }
    }
}
