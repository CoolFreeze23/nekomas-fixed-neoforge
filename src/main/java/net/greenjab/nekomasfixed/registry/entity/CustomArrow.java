package net.greenjab.nekomasfixed.registry.entity;

import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CustomArrow extends Arrow {
    public CustomArrow(final EntityType<? extends CustomArrow> type, final Level level) {
        super(type, level);
    }

    public CustomArrow(final Level level, final double x, final double y, final double z, final ItemStack pickupItemStack) {
        super(ArcheryRegistry.CUSTOM_ARROW, level);
        this.setPos(x, y, z);
        this.setPickupItemStack(pickupItemStack);
    }
}
