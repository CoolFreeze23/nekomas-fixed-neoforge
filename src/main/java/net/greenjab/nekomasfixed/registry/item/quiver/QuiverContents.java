package net.greenjab.nekomasfixed.registry.item.quiver;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class QuiverContents implements TooltipComponent {
    public static final QuiverContents EMPTY = new QuiverContents(List.of());
    public static final Codec<QuiverContents> CODEC = ItemStack.CODEC.listOf().xmap(QuiverContents::new, QuiverContents::items);
    public static final StreamCodec<RegistryFriendlyByteBuf, QuiverContents> STREAM_CODEC =
            ItemStack.LIST_STREAM_CODEC.map(QuiverContents::new, QuiverContents::items);

    public static final int MAX_ARROWS = 9 * 64;
    public static final int NO_SELECTED_ITEM_INDEX = -1;

    private final ImmutableList<ItemStack> items;
    private final int selectedItem;

    private QuiverContents(final List<ItemStack> items, final int selectedItem) {
        this.items = ImmutableList.copyOf(items);
        this.selectedItem = selectedItem;
    }

    public QuiverContents(final List<ItemStack> items) {
        this(items, NO_SELECTED_ITEM_INDEX);
    }

    public static boolean canItemBeInQuiver(final ItemStack itemToAdd) {
        return !itemToAdd.isEmpty()
                && itemToAdd.getItem() instanceof ArrowItem
                && itemToAdd.getItem().canFitInsideContainerItems();
    }

    public int getNumberOfItemsToShow() {
        int numberOfItemStacks = this.size();
        int availableItemsToShow = numberOfItemStacks > 12 ? 11 : 12;
        int itemsOnNonFullRow = numberOfItemStacks % 4;
        int emptySpaceOnNonFullRow = itemsOnNonFullRow == 0 ? 0 : 4 - itemsOnNonFullRow;
        return Math.min(numberOfItemStacks, availableItemsToShow - emptySpaceOnNonFullRow);
    }

    public Stream<ItemStack> itemCopyStream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public List<ItemStack> items() {
        return this.items;
    }

    public int size() {
        return this.items.size();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public int getSelectedItemIndex() {
        return this.selectedItem;
    }

    public @Nullable ItemStack getSelectedItem() {
        return this.selectedItem == NO_SELECTED_ITEM_INDEX ? null : this.items.get(this.selectedItem);
    }

    public int getTotalArrowCount() {
        int total = 0;
        for (ItemStack stack : this.items) {
            total += stack.getCount();
        }
        return total;
    }

    public boolean doesItemHasPlace(final ItemStack itemStack) {
        return Math.min(itemStack.getCount(), MAX_ARROWS - getTotalArrowCount()) == itemStack.getCount();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj instanceof QuiverContents other) {
            return ItemStack.listMatches(this.items, other.items) && this.selectedItem == other.selectedItem;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * ItemStack.hashStackList(this.items) + selectedItem;
    }

    @Override
    public String toString() {
        return "QuiverContents" + items;
    }

    public static class Mutable {
        public final List<ItemStack> items;
        private int selectedItem;

        public Mutable(final QuiverContents contents) {
            this.items = new ArrayList<>(contents.items.size());
            for (ItemStack stack : contents.items) {
                this.items.add(stack.copy());
            }
            this.selectedItem = contents.selectedItem;
        }

        public Mutable clearItems() {
            this.items.clear();
            this.selectedItem = NO_SELECTED_ITEM_INDEX;
            return this;
        }

        private int findStackIndex(final ItemStack itemsToAdd) {
            if (!itemsToAdd.isStackable()) return -1;
            for (int i = 0; i < this.items.size(); i++) {
                if (ItemStack.isSameItemSameComponents(this.items.get(i), itemsToAdd)) {
                    return i;
                }
            }
            return -1;
        }

        public int getCountOfStack(final ItemStack itemToSearch) {
            int count = 0;
            for (ItemStack stack : items) {
                if (stack.is(itemToSearch.getItem())) {
                    count += stack.getCount();
                }
            }
            return count;
        }

        private int remainingSpace() {
            return MAX_ARROWS - this.toImmutable().getTotalArrowCount();
        }

        /** Moves as many arrows as fit out of the given stack, and returns how many moved. */
        public int tryInsert(final ItemStack itemsToAdd) {
            if (!QuiverContents.canItemBeInQuiver(itemsToAdd)) return 0;

            int amountToAdd = Math.min(itemsToAdd.getCount(), this.remainingSpace());
            if (amountToAdd <= 0) return 0;
            int inserted = amountToAdd;

            int index = this.findStackIndex(itemsToAdd);
            if (index != -1) {
                ItemStack existing = this.items.get(index);
                int spaceInStack = existing.getMaxStackSize() - existing.getCount();
                if (spaceInStack > 0) {
                    int addToExisting = Math.min(amountToAdd, spaceInStack);
                    existing.grow(addToExisting);
                    amountToAdd -= addToExisting;
                    itemsToAdd.shrink(addToExisting);
                }
            }

            while (amountToAdd > 0) {
                int toPlace = Math.min(amountToAdd, 64);
                ItemStack newStack = itemsToAdd.split(toPlace);
                this.items.add(0, newStack);
                amountToAdd -= toPlace;
            }

            return inserted;
        }

        public int tryTransfer(final Slot slot, final Player player) {
            ItemStack other = slot.getItem();
            if (QuiverContents.canItemBeInQuiver(other)) {
                int maxAmount = Math.min(other.getCount(), this.remainingSpace());
                return this.tryInsert(slot.safeTake(other.getCount(), maxAmount, player));
            }
            return 0;
        }

        public void toggleSelectedItem(final int selectedItem) {
            this.selectedItem = (this.selectedItem != selectedItem && !indexIsOutsideAllowedBounds(selectedItem))
                    ? selectedItem
                    : NO_SELECTED_ITEM_INDEX;
        }

        private boolean indexIsOutsideAllowedBounds(final int index) {
            return index < 0 || index >= this.items.size();
        }

        public @Nullable ItemStack removeOne() {
            if (this.items.isEmpty()) return null;
            int removeIndex = this.indexIsOutsideAllowedBounds(this.selectedItem) ? 0 : this.selectedItem;
            ItemStack removed = this.items.remove(removeIndex).copy();
            this.toggleSelectedItem(NO_SELECTED_ITEM_INDEX);
            return removed;
        }

        public QuiverContents toImmutable() {
            ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
            for (ItemStack stack : this.items) {
                if (!stack.isEmpty()) {
                    builder.add(stack.copy());
                }
            }
            return new QuiverContents(builder.build(), this.selectedItem);
        }
    }
}
