package net.greenjab.nekomasfixed.screen;

import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.Set;

public class KilnRecipeBookComponent extends AbstractFurnaceRecipeBookComponent {
    private static final Component TOGGLE_KILNABLE_TEXT = Component.translatable("gui.recipebook.toggleRecipes.kilnable");

    @Override
    protected Component getRecipeFilterName() {
        return TOGGLE_KILNABLE_TEXT;
    }

    @Override
    protected Set<Item> getFuelItems() {
        return AbstractFurnaceBlockEntity.getFuel().keySet();
    }
}
