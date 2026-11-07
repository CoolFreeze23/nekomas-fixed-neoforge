package net.greenjab.nekomasfixed.registry.item;

import net.greenjab.nekomasfixed.registry.entity.CustomArrow;
import net.greenjab.nekomasfixed.registry.other.TippedArrowCustomComponent;
import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CustomArrowItem extends ArrowItem {
    public CustomArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public Projectile asProjectile(final Level level, final Position position, final ItemStack itemStack, final Direction direction) {
        CustomArrow arrow = new CustomArrow(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, tooltip, tooltipFlag);
        TippedArrowCustomComponent component = itemStack.get(ArcheryRegistry.TIPPED_POTION_CONTENTS);
        if (component != null) {
            component.addToTooltip(context, tooltip::add, tooltipFlag);
        }
    }
}
