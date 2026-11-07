package net.greenjab.nekomasfixed.registry.worldgen;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.worldgen.feature.ClamFeature;
import net.greenjab.nekomasfixed.registry.worldgen.feature.GeyserBlockFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

/** Features go into biomes through data/nekomasfixed/neoforge/biome_modifier. */
public class ModWorldGeneration {

    @SuppressWarnings("unused")
    public static final Feature<CountConfiguration> CLAM_FEATURE =
            Registry.register(BuiltInRegistries.FEATURE, NekomasFixed.id("clam"),
                    new ClamFeature(CountConfiguration.CODEC));

    @SuppressWarnings("unused")
    public static final GeyserBlockFeature GEYSER_FEATURE =
            Registry.register(BuiltInRegistries.FEATURE, NekomasFixed.id("geyser_feature"),
                    new GeyserBlockFeature(SimpleBlockConfiguration.CODEC));

    public static void generateModWorldGen() {
    }
}
