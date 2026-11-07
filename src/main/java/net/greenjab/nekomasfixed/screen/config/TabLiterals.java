package net.greenjab.nekomasfixed.screen.config;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.MapColor;

public class TabLiterals {

    private static Component text(String id, String name) {
        return Component.translatable("config." + id + ".button." + name);
    }

    private static Component desc(String id, String name) {
        return Component.translatable("config." + id + ".desc." + name);
    }

    private static Component separator(String name, int color) {
        return Component.translatable("config.separator." + name).withColor(color);
    }

    private static Component title(String name) {
        return Component.translatable("config.main_title." + name);
    }

    private static Component goTo(String name) {
        return Component.translatable("config.go_to." + name);
    }

    public static final Component MAIN_TITLE = Component.empty()
            .append(Component.translatable("config.nekoma").withColor(0xFF0054).withStyle(ChatFormatting.BOLD))
            .append(Component.literal(" "))
            .append(Component.translatable("config.fixed").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));

    // TITLES, HEADERS AND SEPARATORS
    public static final Component IN_GAME = Component.translatable("config.in_game");
    public static final Component MOD_CREDITS = Component.translatable("credits.button.credits");

    public static final Component NETHER_TITLE = title("nether");
    public static final Component ENTITY_TITLE = title("entity");
    public static final Component ITEM_TITLE = title("item");
    public static final Component BLOCKS_TITLE = title("blocks");
    public static final Component WORLD_MENU_TITLE = title("worldgen");
    public static final Component CREDITS = title("credits");

    public static final Component CANNOT_BE_CHANGED = Component.translatable("config.separator.cannot_be_changed").withStyle(ChatFormatting.YELLOW);

    public static final Component TERRAIN = separator("terrain", 0xBA2720);
    public static final Component NETHER = separator("nether", 0xBA2720);
    public static final Component VILLAGERS = separator("villagers", 0xBA2720);
    public static final Component SPAWN = separator("spawn", 0xFFFF55);
    public static final Component RECIPE = separator("recipe", 0x55FFFF);
    public static final Component INTERACTION = separator("interaction", 0x55FF55);
    public static final Component DEVELOPERS = separator("developers", MapColor.COLOR_RED.col);
    public static final Component ASSET_PROVIDERS = separator("asset_providers", MapColor.COLOR_LIGHT_GRAY.col);
    public static final Component TESTERS_CREDITS = separator("testers", MapColor.COLOR_PURPLE.col);
    public static final Component TRANSLATORS_CREDITS = separator("translators", MapColor.COLOR_PURPLE.col);

    // BUTTON TEXTS
    public static final Component GO_TO_WORLDGEN = goTo("worldgen");
    public static final Component GO_TO_ENTITY = goTo("entity");
    public static final Component GO_TO_NETHER = goTo("nether");
    public static final Component GO_TO_ITEM = goTo("item");
    public static final Component GO_TO_BLOCKS = goTo("blocks");

    // SUB CATEGORY TEXTS
    public static final Component CAKE = Component.translatable("config.sub_category.cake");

    // WORLDGEN OPTIONS
    public static final Component GENERATE_GEYSERS = text("worldgen", "gen_geyser");
    public static final Component GENERATE_GEYSERS_DESC = desc("worldgen", "gen_geyser");
    public static final Component GENERATE_TERMITE_MOUND = text("worldgen", "gen_mound");
    public static final Component GENERATE_TERMITE_MOUND_DESC = desc("worldgen", "gen_mound");

    // NETHER OPTIONS
    public static final Component DO_FOOD_ROTTING = text("nether", "do_food_rotting");
    public static final Component DO_FOOD_ROTTING_DESC = desc("nether", "do_food_rotting");
    public static final Component MAGMA_BLOCK_CHANGES = text("nether", "magma_block_changes");
    public static final Component MAGMA_BLOCK_CHANGES_DESC = desc("nether", "magma_block_changes");

    // ITEM OPTIONS
    public static final Component ENABLE_DYED_BRUSHES = text("other", "enable_dyed_brushes");
    public static final Component ENABLE_DYED_BRUSHES_DESC = desc("other", "enable_dyed_brushes");

    // BLOCK OPTIONS
    public static final Component ENABLE_STACKED_CAKES = text("blocks", "enable_stacked_cakes");
    public static final Component ENABLE_STACKED_CAKES_DESC = desc("blocks", "enable_stacked_cakes");
    public static final Component ENDERMAN_HEAD_ACTIVATES = text("blocks", "enderman_head_activates");
    public static final Component ENDERMAN_HEAD_ACTIVATES_DESC = desc("blocks", "enderman_head_activates");
    public static final Component CAKE_SLIDER = Component.translatable("config.slider.cake_layers");
    public static final Component CAKE_SLIDER_DESC = Component.translatable("config.slider.desc.cake_layers");

    // ENTITY OPTIONS
    public static final Component SPAWN_SUS_SPIDERS = text("entity", "spawn_sus_spider");
    public static final Component SPAWN_SUS_SPIDERS_DESC = desc("entity", "spawn_sus_spider");
    public static final Component SPAWN_MOOBLOOMS = text("entity", "spawn_mooblooms");
    public static final Component SPAWN_MOOBLOOMS_DESC = desc("entity", "spawn_mooblooms");
    public static final Component SPAWN_TERMITE = text("entity", "spawn_termite");
    public static final Component SPAWN_TERMITE_DESC = desc("entity", "spawn_termite");
    public static final Component SPAWN_DRENCHED = text("entity", "spawn_drenched");
    public static final Component SPAWN_DRENCHED_DESC = desc("entity", "spawn_drenched");
    public static final Component SPAWN_RIME = text("entity", "spawn_rime");
    public static final Component SPAWN_RIME_DESC = desc("entity", "spawn_rime");
    public static final Component SPAWN_DERELICT = text("entity", "spawn_derelict");
    public static final Component SPAWN_DERELICT_DESC = desc("entity", "spawn_derelict");
    public static final Component SPAWN_WILDFIRE = text("entity", "spawn_wildfire");
    public static final Component SPAWN_WILDFIRE_DESC = desc("entity", "spawn_wildfire");
    public static final Component ENABLE_MESSY_BEDS = text("entity", "enable_messy_beds");
    public static final Component ENABLE_MESSY_BEDS_DESC = desc("entity", "enable_messy_beds");
}
