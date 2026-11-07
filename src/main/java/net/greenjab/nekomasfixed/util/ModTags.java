package net.greenjab.nekomasfixed.util;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.biome.Biome;

public class ModTags {
    public static final TagKey<Item> STACKED_CAKES = TagKey.create(Registries.ITEM, NekomasFixed.id("stacked_cakes"));
    public static final TagKey<Item> CLAMS = itemTag("clams");
    public static final TagKey<Item> CORALS = itemTag("corals");
    public static final TagKey<Item> SLINGSHOT_PROJECTILES = itemTag("slingshot_projectiles");
    public static final TagKey<Item> SLINGSHOT_ENCHANTABLE = itemTag("enchantable/slingshot");

    // The 7 sickle tiers (wooden..netherite), used for the dual-wield offhand attack.
    public static final TagKey<Item> SICKLES = itemTag("sickles");

    // Wood-family tag: the four baobab log/wood variants that yield planks.
    public static final TagKey<Block> BAOBAB_LOGS = blockTag("baobab_logs");
    public static final TagKey<Item> BAOBAB_LOGS_ITEM = itemTag("baobab_logs");

    // Hollow-log family: all hollow log/stem/block variants (one per wood type).
    public static final TagKey<Block> HOLLOW_LOGS = blockTag("hollow_logs");
    public static final TagKey<Item> HOLLOW_LOGS_ITEM = itemTag("hollow_logs");
    // Stripped hollow logs (axe-stripped variants of the above).
    public static final TagKey<Block> STRIPPED_HOLLOW_LOGS = blockTag("stripped_hollow_logs");
    public static final TagKey<Item> STRIPPED_HOLLOW_LOGS_ITEM = itemTag("stripped_hollow_logs");
    // Moobloom food: the flowers a moobloom eats / is tempted by.
    public static final TagKey<Item> MOOBLOOM_FLOWERS = itemTag("moobloom_flowers");

    // Biomes where Rime spawns (data/nekomasfixed/tags/worldgen/biome/spawns_rime.json).
    public static final TagKey<Biome> SPAWNS_RIME = TagKey.create(Registries.BIOME, NekomasFixed.id("spawns_rime"));

    // Colour-suite tags (block + item, mirroring the committed JSON pairs).
    public static final TagKey<Block> BRICKS = blockTag("bricks");
    public static final TagKey<Block> BRICK_SLABS = blockTag("brick_slabs");
    public static final TagKey<Block> BRICK_STAIRS = blockTag("brick_stairs");
    public static final TagKey<Block> BRICK_WALLS = blockTag("brick_walls");
    public static final TagKey<Block> SPOTTED_WOOL = blockTag("spotted_wool");
    public static final TagKey<Block> SPOTTED_CARPET = blockTag("spotted_carpet");
    public static final TagKey<Block> FROGLIGHTS = blockTag("froglights");
    public static final TagKey<Block> GLAZED_TERRACOTTAS = blockTag("glazed_terracottas");

    public static final TagKey<Item> BRICKS_ITEM = itemTag("bricks");
    public static final TagKey<Item> BRICK_SLABS_ITEM = itemTag("brick_slabs");
    public static final TagKey<Item> BRICK_STAIRS_ITEM = itemTag("brick_stairs");
    public static final TagKey<Item> BRICK_WALLS_ITEM = itemTag("brick_walls");
    public static final TagKey<Item> SPOTTED_WOOL_ITEM = itemTag("spotted_wool");
    public static final TagKey<Item> SPOTTED_CARPET_ITEM = itemTag("spotted_carpet");
    public static final TagKey<Item> FROGLIGHTS_ITEM = itemTag("froglights");

    // Dyed brush and terracotta pot tags.
    public static final TagKey<Block> CAN_BE_DYED_WITH_BRUSH = blockTag("can_be_dyed_with_brush");
    public static final TagKey<Block> DYED_BRICKS = blockTag("dyed_bricks");
    public static final TagKey<Block> DYED_BRICK_SLABS = blockTag("dyed_brick_slabs");
    public static final TagKey<Block> DYED_BRICK_STAIRS = blockTag("dyed_brick_stairs");
    public static final TagKey<Block> DYED_BRICK_WALLS = blockTag("dyed_brick_walls");
    public static final TagKey<Block> STAINED_GLASSES = blockTag("stained_glasses");
    public static final TagKey<Block> STAINED_GLASS_PANES = blockTag("stained_glass_panes");
    public static final TagKey<Block> CONCRETES = blockTag("concretes");
    public static final TagKey<Block> CONCRETE_POWDERS = blockTag("concrete_powders");
    public static final TagKey<Block> SPOTTED_WOOLS = blockTag("spotted_wools");
    public static final TagKey<Block> SPOTTED_CARPETS = blockTag("spotted_carpets");
    public static final TagKey<Block> SPOTTED_WOOL_SLABS = blockTag("spotted_wool_slabs");
    public static final TagKey<Block> SPOTTED_WOOL_STAIRS = blockTag("spotted_wool_stairs");
    public static final TagKey<Block> WOOL_SLABS = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("wool_slabs"));
    public static final TagKey<Block> WOOL_STAIRS = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("wool_stairs"));
    public static final TagKey<Block> CONCRETE_SLABS = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("concrete_slabs"));
    public static final TagKey<Block> CONCRETE_STAIRS = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("concrete_stairs"));
    public static final TagKey<Block> CAN_BECOME_POT_FACE = blockTag("can_become_pot_face");
    public static final TagKey<Block> SULFUR_BLOCKS = blockTag("sulfur_blocks");
    public static final TagKey<Block> TORCHES = blockTag("torches");
    public static final TagKey<Item> CAN_BE_ENGRAVED = itemTag("can_be_engraved");

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, NekomasFixed.id(path));
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, NekomasFixed.id(path));
    }
}