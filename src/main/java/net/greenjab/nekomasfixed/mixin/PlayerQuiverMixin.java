package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.item.quiver.QuiverContents;
import net.greenjab.nekomasfixed.registry.item.quiver.QuiverItem;
import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerQuiverMixin {

    // Arrows in hand come first, then a quiver's first stack, then the rest of the inventory.
    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void getProjectileFromQuiver(ItemStack heldWeapon, CallbackInfoReturnable<ItemStack> cir) {
        if (!(heldWeapon.getItem() instanceof ProjectileWeaponItem weapon)) {
            return;
        }
        Player player = (Player) (Object) this;
        if (!ProjectileWeaponItem.getHeldProjectile(player, weapon.getSupportedHeldProjectiles(heldWeapon)).isEmpty()) {
            return;
        }

        Predicate<ItemStack> supportedAll = weapon.getAllSupportedProjectiles(heldWeapon);
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof QuiverItem) {
                QuiverContents contents = stack.get(ArcheryRegistry.QUIVER_CONTENTS);
                if (contents == null || contents.isEmpty()) continue;

                Optional<ItemStack> firstArrow = contents.itemCopyStream().findFirst();
                if (firstArrow.isPresent() && supportedAll.test(firstArrow.get())) {
                    cir.setReturnValue(CommonHooks.getProjectile(player, heldWeapon, firstArrow.get()));
                    return;
                }
            }
        }
    }
}
