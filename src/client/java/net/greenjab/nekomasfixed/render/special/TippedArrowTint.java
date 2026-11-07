package net.greenjab.nekomasfixed.render.special;

import net.greenjab.nekomasfixed.registry.other.TippedArrowCustomComponent;
import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.greenjab.nekomasfixed.util.ColorMerger;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

public class TippedArrowTint implements ItemColor {
    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        if (tintIndex != 1) {
            return -1;
        }
        TippedArrowCustomComponent comp = itemStack.get(ArcheryRegistry.TIPPED_POTION_CONTENTS);
        return comp == null ? 0xFFFFFFFF : ColorMerger.mix(comp.getColorsForPotions());
    }
}
