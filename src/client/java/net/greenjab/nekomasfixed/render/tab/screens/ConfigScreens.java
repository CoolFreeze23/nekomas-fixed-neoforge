package net.greenjab.nekomasfixed.render.tab.screens;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.greenjab.nekomasfixed.screen.config.TabLiterals;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.MapColor;

public final class ConfigScreens {
    private ConfigScreens() {
    }

    public static Screen worldGen(Screen parent) {
        return new OptionsListScreen(parent, TabLiterals.WORLD_MENU_TITLE, page -> page
                .text(TabLiterals.CANNOT_BE_CHANGED)
                .text(TabLiterals.TERRAIN)
                .toggle(TabLiterals.GENERATE_GEYSERS, TabLiterals.GENERATE_GEYSERS_DESC, ModConfigValues.generateGeysers)
                .toggle(TabLiterals.GENERATE_TERMITE_MOUND, TabLiterals.GENERATE_TERMITE_MOUND_DESC, ModConfigValues.generateMound));
    }

    public static Screen nether(Screen parent) {
        return new OptionsListScreen(parent, TabLiterals.NETHER_TITLE, page -> page
                .text(TabLiterals.NETHER)
                .toggle(TabLiterals.DO_FOOD_ROTTING, TabLiterals.DO_FOOD_ROTTING_DESC, ModConfigValues.netherFoodRotting)
                .toggle(TabLiterals.MAGMA_BLOCK_CHANGES, TabLiterals.MAGMA_BLOCK_CHANGES_DESC, ModConfigValues.magmaBlockNeedsSilkTouch));
    }

    public static Screen entity(Screen parent) {
        return new OptionsListScreen(parent, TabLiterals.ENTITY_TITLE, page -> page
                .text(TabLiterals.SPAWN)
                .toggle(TabLiterals.SPAWN_SUS_SPIDERS, TabLiterals.SPAWN_SUS_SPIDERS_DESC, ModConfigValues.spawnSusSpider)
                .toggle(TabLiterals.SPAWN_MOOBLOOMS, TabLiterals.SPAWN_MOOBLOOMS_DESC, ModConfigValues.spawnMoobloom)
                .toggle(TabLiterals.SPAWN_TERMITE, TabLiterals.SPAWN_TERMITE_DESC, ModConfigValues.spawnTermite)
                .toggle(TabLiterals.SPAWN_DERELICT, TabLiterals.SPAWN_DERELICT_DESC, ModConfigValues.spawnDerelict)
                .toggle(TabLiterals.SPAWN_RIME, TabLiterals.SPAWN_RIME_DESC, ModConfigValues.spawnRime)
                .toggle(TabLiterals.SPAWN_DRENCHED, TabLiterals.SPAWN_DRENCHED_DESC, ModConfigValues.spawnDrenched)
                .toggle(TabLiterals.SPAWN_WILDFIRE, TabLiterals.SPAWN_WILDFIRE_DESC, ModConfigValues.spawnWildfire));
    }

    public static Screen item(Screen parent) {
        return new OptionsListScreen(parent, TabLiterals.ITEM_TITLE, page -> page
                .text(TabLiterals.RECIPE)
                .toggle(TabLiterals.ENABLE_DYED_BRUSHES, TabLiterals.ENABLE_DYED_BRUSHES_DESC, ModConfigValues.dyedBrush));
    }

    public static Screen blocks(Screen parent) {
        return new OptionsListScreen(parent, TabLiterals.BLOCKS_TITLE, page -> page
                .text(TabLiterals.VILLAGERS)
                .toggle(TabLiterals.ENABLE_MESSY_BEDS, TabLiterals.ENABLE_MESSY_BEDS_DESC, ModConfigValues.enableMessyBeds)
                .text(TabLiterals.INTERACTION)
                .toggle(TabLiterals.ENDERMAN_HEAD_ACTIVATES, TabLiterals.ENDERMAN_HEAD_ACTIVATES_DESC, ModConfigValues.endermanHeadActivates)
                .text(TabLiterals.CAKE)
                .toggle(TabLiterals.ENABLE_STACKED_CAKES, TabLiterals.ENABLE_STACKED_CAKES_DESC, ModConfigValues.enableStackedCakes)
                .slider(TabLiterals.CAKE_SLIDER, TabLiterals.CAKE_SLIDER_DESC, ModConfigValues.cakeSize, 2, 3));
    }

    public static Screen credits(Screen parent) {
        return new OptionsListScreen(parent, TabLiterals.CREDITS, page -> {
            page.text(TabLiterals.DEVELOPERS);
            credit(page, "green_jab", "Green_Jab", 0x55FF55);
            credit(page, "cybermodder", "CyberModder", MapColor.COLOR_ORANGE.col);
            credit(page, "akshaj", "Akshaj", MapColor.COLOR_PURPLE.col);
            credit(page, "technicman", "Technicman", MapColor.COLOR_YELLOW.col);
            credit(page, "strikey", "Strikey", 0x00FFFF);
            credit(page, "krisendo", "Krisendo", 0x771C1F);
            credit(page, "tejty", "Tejty", 0x40BCBE);
            page.text(TabLiterals.ASSET_PROVIDERS);
            credit(page, "velocyblue", "VelocyBlue", 0x302E76);
            credit(page, "lebeno", "Lebeno", 0x9EAEAF);
            page.text(TabLiterals.TESTERS_CREDITS);
            credit(page, "toaster", "Wifi Toaster", 0xFF55FF);
            credit(page, "speedy", "Speedy", MapColor.COLOR_LIGHT_BLUE.col);
            credit(page, "ditto", "Ditto_EXE20", MapColor.GOLD.col);
            page.text(TabLiterals.TRANSLATORS_CREDITS);
            credit(page, "akshaj", "Akshaj", MapColor.COLOR_PURPLE.col);
            credit(page, "alvarosebb", "AlvaroSebb", 0x83141E);
            credit(page, "limo4317", "Limo4317", 0xFFFF55);
            credit(page, "limurchik14", "Limurchik14", 0xFFFF55);
            credit(page, "nojmisek", "Nojmisek", MapColor.TERRACOTTA_RED.col);
            credit(page, "parasitagamer_ofc", "Parasitagamer_ofc", 0xFF5555);
            credit(page, "sk4rdy", "SK4RDY", MapColor.TERRACOTTA_WHITE.col);
            credit(page, "technicman", "Technicman", MapColor.COLOR_YELLOW.col);
            credit(page, "toto77", "TOTO77", 0x55FF55);
            credit(page, "zloykoshak34", "ZloyKoshak34", MapColor.TERRACOTTA_BLACK.col);
            page.text(Component.literal("Nekoma").withStyle(ChatFormatting.BOLD).withColor(0xFF0054));
            credit(page, "nekoma", "Nekoma", 0xFF0054);
        });
    }

    private static void credit(OptionsListScreen page, String image, String name, int color) {
        page.credit(NekomasFixed.id("textures/gui/sprites/images/" + image + ".png"), Component.literal(name).withColor(color));
    }
}
