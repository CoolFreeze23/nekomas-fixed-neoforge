package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    // Tipping takes the whole stack of arrows, so taking the result clears the arrows and leaves the bottles.
    @Inject(method = "clicked", at = @At("HEAD"))
    private void removeArrowsAndPlaceGlassBottles(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        int gridEnd;
        if (menu instanceof CraftingMenu) {
            gridEnd = 10;
        } else if (menu instanceof InventoryMenu) {
            gridEnd = 5;
        } else {
            return;
        }

        if (slotIndex == 0 && menu.getSlot(0).getItem().is(ArcheryRegistry.TIPPED_ARROW_CUSTOM)) {
            ItemStack result = menu.getSlot(0).getItem();
            player.getInventory().add(result);
            if (!result.isEmpty()) {
                if (!player.level().isClientSide()) {
                    player.drop(result.copy(), false);
                }
                result.setCount(0);
            }
            for (int i = 1; i < gridEnd; i++) {
                Slot slot = menu.getSlot(i);
                ItemStack stack = slot.getItem();
                if (stack.is(Items.ARROW)) {
                    slot.set(ItemStack.EMPTY);
                }
                if (stack.is(Items.POTION)) {
                    slot.set(Items.GLASS_BOTTLE.getDefaultInstance());
                }
            }
        }
    }
}
