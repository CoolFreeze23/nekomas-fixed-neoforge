package net.greenjab.nekomasfixed.render.other;

import net.greenjab.nekomasfixed.registry.other.AnimalComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.NonNull;

@OnlyIn(Dist.CLIENT)
public class AnimalTooltipComponent implements ClientTooltipComponent {
    private final AnimalComponent animalComponent;

    public AnimalTooltipComponent(AnimalComponent animalComponent) {
        this.animalComponent = animalComponent;
    }

    @Override
    public int getHeight() {
        if (animalComponent.animal().isEmpty()) return 0;
        return 3 * 24;
    }

    @Override
    public int getWidth(@NonNull Font textRenderer) {
        if (animalComponent.animal().isEmpty()) return 0;
        return 3 * 24;
    }

    @Override
    public void renderImage(@NonNull Font textRenderer, int x, int y, @NonNull GuiGraphics context) {
        Level level = Minecraft.getInstance().level;
        if (level != null && !animalComponent.animal().isEmpty()) {
            Entity entity = animalComponent.animal().getFirst().loadEntity(level);
            if (entity instanceof LivingEntity living) {
                entity.tickCount = (int) level.getGameTime();
                float time = System.currentTimeMillis() % (20 * 1000);
                time *= (float) (2 * Math.PI) / (20 * 1000.0f);
                float dx = 10 * (float) (Math.cos(7 * time) + Math.sin(3 * time));
                float dy = 10 * (float) (Math.cos(5 * time) + Math.sin(2 * time));
                InventoryScreen.renderEntityInInventoryFollowsMouse(context, x, y - 20, x + getWidth(textRenderer), y + getHeight(), 40, 0.25F, x - 15 + dx, y + 30 + dy, living);
            }
        }
    }
}
