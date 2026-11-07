package net.greenjab.nekomasfixed.render.entity.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.greenjab.nekomasfixed.registries.TermiteCrownClient;
import net.greenjab.nekomasfixed.registry.item.GoatHornHelmetItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.NonNull;

@OnlyIn(Dist.CLIENT)
public class GoatHornHelmetLayer<T extends LivingEntity, M extends HumanoidModel<T>>
        extends RenderLayer<T, M> {

    private final ModelPart helmetPart;
    private final ModelPart helmetPartTrim;
    private final TextureAtlas armorTrimAtlas;

    public GoatHornHelmetLayer(RenderLayerParent<T, M> context, EntityModelSet entityModels, ModelManager modelManager) {
        super(context);
        ModelPart baked = entityModels.bakeLayer(TermiteCrownClient.GOAT_HORN_HELMET);
        this.helmetPart = baked.getChild("head").getChild("Helmet");

        ModelPart bakedTrim = entityModels.bakeLayer(TermiteCrownClient.GOAT_HORN_HELMET_TRIM);
        this.helmetPartTrim = bakedTrim.getChild("head").getChild("Helmet");

        this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    }

    /** Moves to the wearer's head the way the vanilla head layer does, babies included. */
    static void translateToHead(PoseStack poseStack, LivingEntity entity, HumanoidModel<?> model) {
        if (entity.isBaby()) {
            poseStack.translate(0.0F, 0.03125F, 0.0F);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            poseStack.translate(0.0F, 1.0F, 0.0F);
        }
        model.head.translateAndRotate(poseStack);
    }

    @Override
    public void render(@NonNull PoseStack poseStack, @NonNull MultiBufferSource buffer, int light, @NonNull T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!(headStack.getItem() instanceof GoatHornHelmetItem helmet)) return;

        poseStack.pushPose();
        translateToHead(poseStack, entity, this.getParentModel());

        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);

        ResourceLocation texture = helmet.getTier().getTexture().withSuffix(".png");
        this.helmetPart.render(poseStack, buffer.getBuffer(RenderType.entityCutout(texture)), light, overlay);

        ArmorTrim trim = headStack.get(DataComponents.TRIM);
        if (trim != null) {
            TextureAtlasSprite sprite = this.armorTrimAtlas.getSprite(trim.outerTexture(helmet.getMaterial()));

            poseStack.pushPose();
            poseStack.scale(1.001F, 1.001F, 1.001F);
            this.helmetPartTrim.render(poseStack,
                    sprite.wrap(buffer.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal()))),
                    light, overlay);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
