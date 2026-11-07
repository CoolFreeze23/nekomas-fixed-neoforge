package net.greenjab.nekomasfixed.registry.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.other.GlintColor;
import net.greenjab.nekomasfixed.util.AllDyes;
import net.greenjab.nekomasfixed.util.GlintColors;
import net.greenjab.nekomasfixed.util.TreasureGlints;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class GlintColorRegistry {
    public static final Map<AllDyes, ResourceKey<GlintColor>> GLINT_COLORS;
    public static final Map<TreasureGlints, ResourceKey<GlintColor>> TREASURE_GLINTS;

    static {
        Map<AllDyes, ResourceKey<GlintColor>> map = new EnumMap<>(AllDyes.class);
        for (AllDyes dye : AllDyes.values()) {
            map.put(dye, ResourceKey.create(GlintColor.REGISTRY_KEY, NekomasFixed.id(dye.getSerializedName())));
        }
        GLINT_COLORS = Collections.unmodifiableMap(map);
        Map<TreasureGlints, ResourceKey<GlintColor>> treasureMap = new EnumMap<>(TreasureGlints.class);
        for (TreasureGlints glint : TreasureGlints.values()) {
            treasureMap.put(glint, ResourceKey.create(GlintColor.REGISTRY_KEY, NekomasFixed.id(glint.getSerializedName())));
        }
        TREASURE_GLINTS = Collections.unmodifiableMap(treasureMap);
    }

    public static void registerGlintColors(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(GlintColor.REGISTRY_KEY, GlintColor.DIRECT_CODEC, GlintColor.DIRECT_CODEC);
    }

    public static int tooltipColor(int color) {
        return GlintColors.lift(color, GlintColors.TOOLTIP_FLOOR, GlintColors.TOOLTIP_FLOOR_GRAY);
    }
}
