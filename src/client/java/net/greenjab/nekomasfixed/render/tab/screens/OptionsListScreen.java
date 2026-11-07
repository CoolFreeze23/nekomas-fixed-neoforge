package net.greenjab.nekomasfixed.render.tab.screens;

import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.greenjab.nekomasfixed.util.options.Option;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** A scrolling page of option rows, separators and credit lines, using only vanilla widgets. */
public class OptionsListScreen extends Screen {
    private static final int ROW_WIDTH = 310;

    private final Screen parent;
    private final Consumer<OptionsListScreen> content;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final List<Row> rows = new ArrayList<>();
    private RowList list;

    public OptionsListScreen(Screen parent, Component title, Consumer<OptionsListScreen> content) {
        super(title);
        this.parent = parent;
        this.content = content;
    }

    public OptionsListScreen text(Component text) {
        rows.add(new TextRow(text));
        return this;
    }

    public OptionsListScreen toggle(Component label, Component tooltip, Option<Boolean> option) {
        CycleButton<Boolean> button = CycleButton.onOffBuilder(option.get())
                .withTooltip(value -> Tooltip.create(tooltip))
                .create(0, 0, ROW_WIDTH, 20, label, (widget, value) -> option.set(value));
        rows.add(new WidgetRow(button));
        return this;
    }

    public OptionsListScreen slider(Component label, Component tooltip, Option<Integer> option, int min, int max) {
        rows.add(new WidgetRow(new IntSlider(label, tooltip, option, min, max)));
        return this;
    }

    public OptionsListScreen credit(ResourceLocation image, Component name) {
        rows.add(new CreditRow(image, name));
        return this;
    }

    @Override
    protected void init() {
        rows.clear();
        content.accept(this);
        layout.addTitleHeader(title, font);
        list = new RowList(minecraft, width, layout.getContentHeight(), layout.getHeaderHeight());
        rows.forEach(list::add);
        layout.addToContents(list);
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(200).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (list != null) {
            list.updateSize(width, layout);
        }
    }

    @Override
    public void onClose() {
        if (ModConfigValues.isWorldLoaded()) {
            ModConfigValues.saveCurrentWorld();
        } else {
            ModConfigValues.applyToNextWorld = true;
        }
        minecraft.setScreen(parent);
    }

    private static final class RowList extends ContainerObjectSelectionList<Row> {
        RowList(Minecraft minecraft, int width, int height, int top) {
            super(minecraft, width, height, top, 25);
        }

        void add(Row row) {
            addEntry(row);
        }

        @Override
        public int getRowWidth() {
            return ROW_WIDTH;
        }
    }

    private abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    private static final class TextRow extends Row {
        private final Component text;

        TextRow(Component text) {
            this.text = text;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.drawCenteredString(font, text, left + width / 2, top + (height - font.lineHeight) / 2, 0xFFFFFF);
        }
    }

    private static final class WidgetRow extends Row {
        private final AbstractWidget widget;

        WidgetRow(AbstractWidget widget) {
            this.widget = widget;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            widget.setX(left);
            widget.setY(top);
            widget.setWidth(width);
            widget.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(widget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(widget);
        }
    }

    private static final class CreditRow extends Row {
        private final ResourceLocation image;
        private final Component name;

        CreditRow(ResourceLocation image, Component name) {
            this.image = image;
            this.name = name;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            Font font = Minecraft.getInstance().font;
            int size = 20;
            graphics.blit(image, left + 80, top, 0, 0, size, size, size, size);
            graphics.drawString(font, name, left + 80 + size + 8, top + (size - font.lineHeight) / 2, 0xFFFFFF);
        }
    }

    private static final class IntSlider extends AbstractSliderButton {
        private final Component label;
        private final Option<Integer> option;
        private final int min;
        private final int max;

        IntSlider(Component label, Component tooltip, Option<Integer> option, int min, int max) {
            super(0, 0, ROW_WIDTH, 20, CommonComponents.EMPTY, (double) (Mth.clamp(option.get(), min, max) - min) / (max - min));
            this.label = label;
            this.option = option;
            this.min = min;
            this.max = max;
            setTooltip(Tooltip.create(tooltip));
            updateMessage();
        }

        private int current() {
            return min + (int) Math.round(value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(CommonComponents.optionNameValue(label, Component.literal(Integer.toString(current())).withStyle(ChatFormatting.WHITE)));
        }

        @Override
        protected void applyValue() {
            option.set(current());
        }
    }
}
