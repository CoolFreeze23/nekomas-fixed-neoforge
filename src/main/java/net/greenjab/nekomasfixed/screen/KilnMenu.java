package net.greenjab.nekomasfixed.screen;

import net.greenjab.nekomasfixed.registry.registries.KilnRegistry;
import net.greenjab.nekomasfixed.util.ModRecipeBookType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;

public class KilnMenu extends AbstractFurnaceMenu {

    public KilnMenu(final int containerId, final Inventory inventory) {
        super(KilnRegistry.KILN_MENU, KilnRegistry.KILN_RECIPE, ModRecipeBookType.KILN, containerId, inventory);
    }

    public KilnMenu(final int containerId, final Inventory inventory, final Container container, final ContainerData data) {
        super(KilnRegistry.KILN_MENU, KilnRegistry.KILN_RECIPE, ModRecipeBookType.KILN, containerId, inventory, container, data);
    }
}
