package net.greenjab.nekomasfixed.render.tab;

import net.greenjab.nekomasfixed.render.tab.screens.ConfigScreens;
import net.greenjab.nekomasfixed.screen.config.TabLiterals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

/** The mod's tab on the Create New World screen. */
public class ModWorldGenOptionTab extends GridLayoutTab {

    public ModWorldGenOptionTab(Screen parent) {
        super(TabLiterals.MAIN_TITLE);
        GridLayout.RowHelper helper = this.layout.rowSpacing(8).createRowHelper(1);
        add(helper, TabLiterals.GO_TO_WORLDGEN, ConfigScreens::worldGen, parent);
        add(helper, TabLiterals.GO_TO_NETHER, ConfigScreens::nether, parent);
        add(helper, TabLiterals.GO_TO_ENTITY, ConfigScreens::entity, parent);
        add(helper, TabLiterals.GO_TO_ITEM, ConfigScreens::item, parent);
        add(helper, TabLiterals.GO_TO_BLOCKS, ConfigScreens::blocks, parent);
    }

    private static void add(GridLayout.RowHelper helper, Component label, Function<Screen, Screen> page, Screen parent) {
        helper.addChild(Button.builder(label, b -> Minecraft.getInstance().setScreen(page.apply(parent))).width(210).build());
    }
}
