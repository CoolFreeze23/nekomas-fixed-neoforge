package net.greenjab.nekomasfixed.render.tab.screens;

import net.greenjab.nekomasfixed.screen.config.TabLiterals;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

/** The options that can still change in a running world, one page per area. */
public class InGameEditScreen extends Screen {
    private final Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public InGameEditScreen(Screen parent) {
        super(TabLiterals.MAIN_TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        layout.addToHeader(new StringWidget(title, font));

        GridLayout grid = layout.addToContents(new GridLayout());
        grid.rowSpacing(8);
        GridLayout.RowHelper helper = grid.createRowHelper(1);
        helper.addChild(Button.builder(TabLiterals.NETHER_TITLE, b -> minecraft.setScreen(ConfigScreens.nether(this))).width(200).build());
        helper.addChild(Button.builder(TabLiterals.ENTITY_TITLE, b -> minecraft.setScreen(ConfigScreens.entity(this))).width(200).build());
        helper.addChild(Button.builder(TabLiterals.ITEM_TITLE, b -> minecraft.setScreen(ConfigScreens.item(this))).width(200).build());
        helper.addChild(Button.builder(TabLiterals.BLOCKS_TITLE, b -> minecraft.setScreen(ConfigScreens.blocks(this))).width(200).build());

        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).width(200).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
