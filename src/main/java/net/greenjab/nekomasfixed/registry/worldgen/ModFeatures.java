package net.greenjab.nekomasfixed.registry.worldgen;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.worldgen.feature.TermiteMoundFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModFeatures {
    public static final ResourceKey<PlacedFeature> MOUND_KEY = ResourceKey.create(Registries.PLACED_FEATURE, NekomasFixed.id("mound"));

    public static final Feature<NoneFeatureConfiguration> MOUND_FEATURE = Registry.register(BuiltInRegistries.FEATURE, NekomasFixed.id("mound"),
            new TermiteMoundFeature(NoneFeatureConfiguration.CODEC));

    public static void register() {
    }
}
