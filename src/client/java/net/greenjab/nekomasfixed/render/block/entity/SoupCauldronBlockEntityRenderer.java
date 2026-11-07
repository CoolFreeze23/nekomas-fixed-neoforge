package net.greenjab.nekomasfixed.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.greenjab.nekomasfixed.registry.block.entity.SoupCauldronBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** The ingredients bob on the water, then swirl down as the soup is stirred. */
public class SoupCauldronBlockEntityRenderer implements BlockEntityRenderer<SoupCauldronBlockEntity> {
    private final ItemRenderer itemRenderer;

    public SoupCauldronBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(SoupCauldronBlockEntity blockEntity, float partialTick, PoseStack matrices, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) return;
        float stirProgress = blockEntity.getOpenNess(partialTick);
        if (stirProgress >= 1) return;
        float animationTime = blockEntity.getLevel().getGameTime() + partialTick;
        int seed = (int) blockEntity.getBlockPos().asLong();
        List<ItemStack> inputs = blockEntity.getInputs();
        for (int i = 0; i < inputs.size(); ++i) {
            matrices.pushPose();
            float bob = (float) Math.sin(animationTime * 0.1f) * 0.02f;
            float stir = stirProgress * stirProgress;
            matrices.translate(0.5F, 1F + bob - stir * 0.2f, 0.5F);
            Direction direction = Direction.from2DDataValue((i + Direction.NORTH.get2DDataValue()) % 4);
            matrices.mulPose(Axis.YN.rotationDegrees(720 * stir - direction.toYRot()));
            matrices.mulPose(Axis.XN.rotationDegrees(-70.0F));
            matrices.translate(-0.23 * (1 - stir), -0.1, 0.0F);
            matrices.scale(0.275F, 0.375F, 0.275F);
            matrices.scale(1 - stir, 1 - stir, 1 - stir);
            this.itemRenderer.renderStatic(inputs.get(i), ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, matrices, buffer, blockEntity.getLevel(), seed + i);
            matrices.popPose();
        }
    }
}
