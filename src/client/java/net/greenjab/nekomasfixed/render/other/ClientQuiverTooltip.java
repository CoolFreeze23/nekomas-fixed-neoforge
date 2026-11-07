package net.greenjab.nekomasfixed.render.other;

import net.greenjab.nekomasfixed.registry.item.quiver.QuiverContents;
import net.greenjab.nekomasfixed.registry.item.quiver.QuiverItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;

/**
 * The grid and fullness bar of the quiver tooltip. The grid sprites of the later bundle tooltip don't exist
 * in 1.21.1, so slots and bar are drawn as flat rectangles in the same layout.
 */
public class ClientQuiverTooltip implements ClientTooltipComponent {
    private static final int SLOT_SIZE = 24;
    private static final int GRID_WIDTH = 96;
    private static final int PROGRESSBAR_HEIGHT = 13;
    private static final int PROGRESSBAR_FILL_MAX = 94;
    private static final int PROGRESSBAR_MARGIN_Y = 4;
    private static final int SLOT_COLOR = 0x50000000;
    private static final int BORDER_COLOR = 0xFF2A2A40;
    private static final Component QUIVER_FULL_TEXT = Component.translatableWithFallback("item.minecraft.bundle.full", "Full");
    private static final Component QUIVER_EMPTY_TEXT = Component.translatableWithFallback("item.minecraft.bundle.empty", "Empty");
    private static final Component QUIVER_EMPTY_DESCRIPTION = Component.translatableWithFallback("item.minecraft.bundle.empty.description", "Can hold a mixed stack of items");
    private final QuiverContents contents;

    public ClientQuiverTooltip(final QuiverContents contents) {
        this.contents = contents;
    }

    @Override
    public int getHeight() {
        return this.contents.isEmpty() ? getEmptyHeight(Minecraft.getInstance().font) : this.itemGridHeight() + PROGRESSBAR_HEIGHT + 2 * PROGRESSBAR_MARGIN_Y;
    }

    @Override
    public int getWidth(final Font font) {
        return GRID_WIDTH;
    }

    private static int getEmptyHeight(final Font font) {
        return getEmptyDescriptionHeight(font) + PROGRESSBAR_HEIGHT + 2 * PROGRESSBAR_MARGIN_Y;
    }

    private static int getEmptyDescriptionHeight(final Font font) {
        return font.split(QUIVER_EMPTY_DESCRIPTION, GRID_WIDTH).size() * 9;
    }

    private int itemGridHeight() {
        return this.gridSizeY() * SLOT_SIZE;
    }

    private int gridSizeY() {
        return Mth.positiveCeilDiv(Math.min(12, this.contents.size()), 4);
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        Fraction weight = Fraction.getFraction(this.contents.getTotalArrowCount(), QuiverContents.MAX_ARROWS);
        if (this.contents.isEmpty()) {
            int textY = y;
            for (FormattedCharSequence line : font.split(QUIVER_EMPTY_DESCRIPTION, GRID_WIDTH)) {
                graphics.drawString(font, line, x, textY, 0xFFAAAAAA, false);
                textY += 9;
            }
            renderProgressbar(x, y + getEmptyDescriptionHeight(font) + PROGRESSBAR_MARGIN_Y, font, graphics, weight);
        } else {
            this.renderGrid(x, y, font, graphics);
            renderProgressbar(x, y + this.itemGridHeight() + PROGRESSBAR_MARGIN_Y, font, graphics, weight);
        }
    }

    private void renderGrid(final int x, final int y, final Font font, final GuiGraphics graphics) {
        boolean isOverflowing = this.contents.size() > 12;
        int shown = this.contents.getNumberOfItemsToShow();
        List<ItemStack> shownItems = this.contents.items().subList(0, Math.min(this.contents.size(), shown));
        int xStartPos = x + GRID_WIDTH;
        int yStartPos = y + this.gridSizeY() * SLOT_SIZE;
        int slotNumber = 1;

        for (int rowNumber = 1; rowNumber <= this.gridSizeY(); rowNumber++) {
            for (int columnNumber = 1; columnNumber <= 4; columnNumber++) {
                int drawX = xStartPos - columnNumber * SLOT_SIZE;
                int drawY = yStartPos - rowNumber * SLOT_SIZE;
                if (isOverflowing && columnNumber * rowNumber == 1) {
                    int hidden = this.contents.items().stream().skip(shownItems.size()).mapToInt(ItemStack::getCount).sum();
                    graphics.drawCenteredString(font, "+" + hidden, drawX + 12, drawY + 10, -1);
                } else if (shownItems.size() >= slotNumber) {
                    ItemStack item = shownItems.get(shownItems.size() - slotNumber);
                    graphics.fill(drawX + 1, drawY + 1, drawX + SLOT_SIZE - 1, drawY + SLOT_SIZE - 1, SLOT_COLOR);
                    graphics.renderItem(item, drawX + 4, drawY + 4, slotNumber);
                    graphics.renderItemDecorations(font, item, drawX + 4, drawY + 4);
                    slotNumber++;
                }
            }
        }
    }

    private static void renderProgressbar(final int x, final int y, final Font font, final GuiGraphics graphics, final Fraction weight) {
        graphics.fill(x, y, x + GRID_WIDTH, y + PROGRESSBAR_HEIGHT, BORDER_COLOR);
        graphics.fill(x + 1, y + 1, x + GRID_WIDTH - 1, y + PROGRESSBAR_HEIGHT - 1, 0xFF000000);
        int fill = Mth.clamp(Mth.mulAndTruncate(weight, PROGRESSBAR_FILL_MAX), 0, PROGRESSBAR_FILL_MAX);
        int color = weight.compareTo(Fraction.ONE) >= 0 ? QuiverItem.FULL_BAR_COLOR : QuiverItem.BAR_COLOR;
        graphics.fill(x + 1, y + 1, x + 1 + fill, y + PROGRESSBAR_HEIGHT - 1, color);
        if (weight.compareTo(Fraction.ZERO) == 0) {
            graphics.drawCenteredString(font, QUIVER_EMPTY_TEXT, x + GRID_WIDTH / 2, y + 3, -1);
        } else if (weight.compareTo(Fraction.ONE) >= 0) {
            graphics.drawCenteredString(font, QUIVER_FULL_TEXT, x + GRID_WIDTH / 2, y + 3, -1);
        }
    }
}
