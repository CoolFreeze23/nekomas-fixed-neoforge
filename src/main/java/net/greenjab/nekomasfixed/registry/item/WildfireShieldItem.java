package net.greenjab.nekomasfixed.registry.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;

public class WildfireShieldItem extends ShieldItem {
    public WildfireShieldItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.NETHERITE_INGOT);
    }
}
