package net.greenjab.nekomasfixed.render.block.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.greenjab.nekomasfixed.registry.other.PotEngravingDecoration;
import net.greenjab.nekomasfixed.registry.other.PotFaceDecoration;
import net.greenjab.nekomasfixed.registry.registries.ComponentRegistry;
import net.greenjab.nekomasfixed.registry.registries.PotRegistry;
import net.greenjab.nekomasfixed.render.block.entity.TerracottaDecoratedPotBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.PotDecorations;

public class TerracottaDecoratedPotItemRenderer extends BlockEntityWithoutLevelRenderer {
    private TerracottaDecoratedPotBlockEntityRenderer renderer;

    public TerracottaDecoratedPotItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.renderer = null;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (this.renderer == null) {
            var models = Minecraft.getInstance().getEntityModels();
            this.renderer = new TerracottaDecoratedPotBlockEntityRenderer(
                    models.bakeLayer(ModelLayers.DECORATED_POT_BASE), models.bakeLayer(TerracottaDecoratedPotBlockEntityRenderer.CUSTOM_POT_SIDES));
        }
        this.renderer.renderPot(new CombinedPotData(
                stack.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY),
                stack.getOrDefault(PotRegistry.POT_ENGRAVED_DECORATIONS, PotEngravingDecoration.EMPTY),
                stack.getOrDefault(PotRegistry.POT_FACE_BLOCK, PotFaceDecoration.FALLBACK_BRICKS),
                stack.getOrDefault(ComponentRegistry.SHERD_GLOW_OVERRIDES, ComponentRegistry.DEFAULT_SHERD_GLOW_OVERRIDES)),
                poseStack, buffer, packedLight, packedOverlay);
    }
}
