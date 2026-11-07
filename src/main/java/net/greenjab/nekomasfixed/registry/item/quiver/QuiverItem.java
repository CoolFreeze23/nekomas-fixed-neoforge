package net.greenjab.nekomasfixed.registry.item.quiver;

import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class QuiverItem extends Item {
    public static final int FULL_BAR_COLOR = FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
    public static final int BAR_COLOR = FastColor.ARGB32.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);

    public QuiverItem(Properties properties) {
        super(properties);
    }

    private static QuiverContents contents(final ItemStack stack) {
        return stack.getOrDefault(ArcheryRegistry.QUIVER_CONTENTS, QuiverContents.EMPTY);
    }

    public static float getFullnessDisplay(final ItemStack itemStack) {
        return contents(itemStack).getTotalArrowCount();
    }

    @Override
    public boolean overrideStackedOnOther(final ItemStack self, final Slot slot, final ClickAction clickAction, final Player player) {
        QuiverContents initialContents = self.get(ArcheryRegistry.QUIVER_CONTENTS);
        if (initialContents == null) {
            return false;
        }
        ItemStack other = slot.getItem();
        QuiverContents.Mutable contents = new QuiverContents.Mutable(initialContents);
        if (clickAction == ClickAction.PRIMARY && !other.isEmpty() && contents.toImmutable().doesItemHasPlace(self)) {
            if (contents.tryTransfer(slot, player) > 0) {
                playInsertSound(player);
            }

            self.set(ArcheryRegistry.QUIVER_CONTENTS, contents.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        } else if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
            ItemStack itemStack = contents.removeOne();
            if (itemStack != null) {
                ItemStack remainder = slot.safeInsert(itemStack);
                if (remainder.getCount() > 0) {
                    contents.tryInsert(remainder);
                } else {
                    playRemoveOneSound(player);
                }
            }

            self.set(ArcheryRegistry.QUIVER_CONTENTS, contents.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(final ItemStack self, final ItemStack other, final Slot slot, final ClickAction clickAction, final Player player, final SlotAccess carriedItem) {
        if (clickAction == ClickAction.PRIMARY && other.isEmpty()) {
            toggleSelectedItem(self, -1);
            return false;
        }
        QuiverContents initialContents = self.get(ArcheryRegistry.QUIVER_CONTENTS);
        if (initialContents == null) {
            return false;
        }
        QuiverContents.Mutable contents = new QuiverContents.Mutable(initialContents);
        if (clickAction == ClickAction.PRIMARY && !other.isEmpty()) {
            if (slot.allowModification(player) && contents.tryInsert(other) > 0) {
                playInsertSound(player);
            }

            self.set(ArcheryRegistry.QUIVER_CONTENTS, contents.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        } else if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
            if (slot.allowModification(player)) {
                ItemStack removed = contents.removeOne();
                if (removed != null) {
                    playRemoveOneSound(player);
                    carriedItem.set(removed);
                }
            }

            self.set(ArcheryRegistry.QUIVER_CONTENTS, contents.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        }
        toggleSelectedItem(self, -1);
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    private void dropContent(final Level level, final Player player, final ItemStack itemStack) {
        if (this.dropContent(itemStack, player)) {
            playDropContentsSound(level, player);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return contents(stack).getTotalArrowCount() > 0;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        return Math.min(1 + Mth.mulAndTruncate(Fraction.getFraction(contents(stack).getTotalArrowCount(), QuiverContents.MAX_ARROWS), 12), 13);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        return contents(stack).getTotalArrowCount() == QuiverContents.MAX_ARROWS ? FULL_BAR_COLOR : BAR_COLOR;
    }

    public static void toggleSelectedItem(final ItemStack stack, final int selectedItem) {
        QuiverContents initialContents = stack.get(ArcheryRegistry.QUIVER_CONTENTS);
        if (initialContents != null) {
            QuiverContents.Mutable contents = new QuiverContents.Mutable(initialContents);
            contents.toggleSelectedItem(selectedItem);
            stack.set(ArcheryRegistry.QUIVER_CONTENTS, contents.toImmutable());
        }
    }

    public static int getSelectedItemIndex(final ItemStack stack) {
        return contents(stack).getSelectedItemIndex();
    }

    public static @Nullable ItemStack getSelectedItem(final ItemStack stack) {
        return contents(stack).getSelectedItem();
    }

    public static int getNumberOfItemsToShow(final ItemStack stack) {
        return contents(stack).getNumberOfItemsToShow();
    }

    private boolean dropContent(final ItemStack quiver, final Player player) {
        QuiverContents contents = quiver.get(ArcheryRegistry.QUIVER_CONTENTS);
        if (contents == null || contents.isEmpty()) {
            return false;
        }
        Optional<ItemStack> itemStack = removeOneStack(quiver, player, contents);
        if (itemStack.isPresent()) {
            player.drop(itemStack.get(), true);
            return true;
        }
        return false;
    }

    private static Optional<ItemStack> removeOneStack(final ItemStack self, final Player player, final QuiverContents initialContents) {
        QuiverContents.Mutable contents = new QuiverContents.Mutable(initialContents);
        ItemStack removed = contents.removeOne();
        if (removed != null) {
            playRemoveOneSound(player);
            self.set(ArcheryRegistry.QUIVER_CONTENTS, contents.toImmutable());
            return Optional.of(removed);
        }
        return Optional.empty();
    }

    // Holding use empties the quiver a stack at a time. Drops happen on the server, the client follows the component sync.
    @Override
    public void onUseTick(final Level level, final LivingEntity livingEntity, final ItemStack itemStack, final int ticksRemaining) {
        if (!level.isClientSide() && livingEntity instanceof Player player) {
            int useDuration = this.getUseDuration(itemStack, livingEntity);
            boolean isFirstTick = ticksRemaining == useDuration;
            if (isFirstTick || ticksRemaining < useDuration - 10 && ticksRemaining % 2 == 0) {
                this.dropContent(level, player, itemStack);
            }
        }
    }

    @Override
    public int getUseDuration(final ItemStack itemStack, final LivingEntity entity) {
        return 200;
    }

    @Override
    public UseAnim getUseAnimation(final ItemStack itemStack) {
        return UseAnim.NONE;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack quiver) {
        QuiverContents contents = quiver.get(ArcheryRegistry.QUIVER_CONTENTS);
        return contents == null ? Optional.empty() : Optional.of(new QuiverTooltip(contents));
    }

    @Override
    public void onDestroyed(final ItemEntity entity) {
        QuiverContents contents = entity.getItem().get(ArcheryRegistry.QUIVER_CONTENTS);
        if (contents != null) {
            entity.getItem().set(ArcheryRegistry.QUIVER_CONTENTS, QuiverContents.EMPTY);
            ItemUtils.onContainerDestroyed(entity, contents.itemCopyStream().toList());
        }
    }

    private static void playRemoveOneSound(final Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(final Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playDropContentsSound(final Level level, final Entity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void broadcastChangesOnContainerMenu(final Player player) {
        AbstractContainerMenu containerMenu = player.containerMenu;
        if (containerMenu != null) {
            containerMenu.slotsChanged(player.getInventory());
        }
    }
}
