package net.greenjab.nekomasfixed.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.block.entity.PotMaps;
import net.greenjab.nekomasfixed.registry.block.entity.TerracottaDecoratedPotBlockEntity;
import net.greenjab.nekomasfixed.registry.block.enums.SpriteFacing;
import net.greenjab.nekomasfixed.registry.registries.GlowingDecoratedPotPatternRegistry;
import net.greenjab.nekomasfixed.render.block.item.CombinedPotData;
import net.greenjab.nekomasfixed.util.GlobalVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.Optional;

public class TerracottaDecoratedPotBlockEntityRenderer implements BlockEntityRenderer<TerracottaDecoratedPotBlockEntity> {
    public static final ModelLayerLocation CUSTOM_POT_SIDES = new ModelLayerLocation(NekomasFixed.id("custom_pot_sides"), "main");
    public static final ResourceLocation CUSTOM_POT = NekomasFixed.id("textures/atlas/custom_pot.png");
    public static final ResourceLocation TERRACOTTA_POT = NekomasFixed.id("textures/atlas/terracotta_decorated_pot.png");

    private final ModelPart neck, frontSide, backSide, leftSide, rightSide;
    private final ModelPart top, bottom;

    public TerracottaDecoratedPotBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this(context.bakeLayer(ModelLayers.DECORATED_POT_BASE), context.bakeLayer(CUSTOM_POT_SIDES));
    }

    public TerracottaDecoratedPotBlockEntityRenderer(ModelPart baseRoot, ModelPart sidesRoot) {
        this.neck = baseRoot.getChild("neck");
        this.top = baseRoot.getChild("top");
        this.bottom = baseRoot.getChild("bottom");
        this.frontSide = sidesRoot.getChild("front");
        this.backSide = sidesRoot.getChild("back");
        this.leftSide = sidesRoot.getChild("left");
        this.rightSide = sidesRoot.getChild("right");
    }

    public static LayerDefinition createSidesLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder sidePlane = CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, EnumSet.of(Direction.NORTH));
        root.addOrReplaceChild("front", sidePlane, PartPose.offsetAndRotation(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, (float) Math.PI));
        root.addOrReplaceChild("back", sidePlane, PartPose.offsetAndRotation(1.0F, 16.0F, 15.0F, (float) Math.PI, 0.0F, 0.0F));
        root.addOrReplaceChild("left", sidePlane, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, (-(float) Math.PI / 2F), (float) Math.PI));
        root.addOrReplaceChild("right", sidePlane, PartPose.offsetAndRotation(15.0F, 16.0F, 15.0F, 0.0F, ((float) Math.PI / 2F), (float) Math.PI));
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void render(TerracottaDecoratedPotBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - blockEntity.getDirection().toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);
        TerracottaDecoratedPotBlockEntity.WobbleStyle wobbleStyle = blockEntity.lastWobbleStyle;
        if (wobbleStyle != null && blockEntity.getLevel() != null) {
            float progress = ((float) (blockEntity.getLevel().getGameTime() - blockEntity.wobbleStartedAtTick) + partialTick) / wobbleStyle.duration;
            if (progress >= 0.0F && progress <= 1.0F) {
                if (wobbleStyle == TerracottaDecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
                    float deltaTime = progress * (float) (Math.PI * 2);
                    float tiltX = -1.5F * (Mth.cos(deltaTime) + 0.5F) * Mth.sin(deltaTime / 2.0F);
                    poseStack.rotateAround(Axis.XP.rotation(tiltX * 0.015625F), 0.5F, 0.0F, 0.5F);
                    poseStack.rotateAround(Axis.ZP.rotation(Mth.sin(deltaTime) * 0.015625F), 0.5F, 0.0F, 0.5F);
                } else {
                    float turnAngle = Mth.sin(-progress * 3.0F * (float) Math.PI) * 0.125F;
                    poseStack.rotateAround(Axis.YP.rotation(turnAngle * (1.0F - progress)), 0.5F, 0.0F, 0.5F);
                }
            }
        }
        renderPot(new CombinedPotData(blockEntity.getDecorations(), blockEntity.getEngravingDecorations(), blockEntity.getPotFace(),
                java.util.List.of(blockEntity.nekomasfixed$getSherdGlow(0), blockEntity.nekomasfixed$getSherdGlow(1),
                        blockEntity.nekomasfixed$getSherdGlow(2), blockEntity.nekomasfixed$getSherdGlow(3))),
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public void renderPot(CombinedPotData data, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Block faceBlock = data.face().getSafeBlock();
        String faceName = PotMaps.POT_FACE_TO_STRING_MAPPING.get(faceBlock);

        // The neck and the face have their own art for bricks and light blue glazed terracotta. Any other face block is drawn with its own block texture.
        VertexConsumer baseBuffer;
        VertexConsumer faceBuffer;
        if (faceName != null) {
            baseBuffer = terracottaPot(faceName, PotMaps.SpritePart.BASE).buffer(buffer, RenderType::entitySolid);
            faceBuffer = terracottaPot(faceName, PotMaps.SpritePart.SIDE).buffer(buffer, RenderType::entitySolid);
        } else {
            TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModel(faceBlock.defaultBlockState()).getParticleIcon();
            faceBuffer = baseBuffer = sprite.wrap(buffer.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)));
        }
        this.neck.render(poseStack, baseBuffer, light, overlay);
        this.top.render(poseStack, baseBuffer, light, overlay);
        this.bottom.render(poseStack, baseBuffer, light, overlay);
        this.leftSide.render(poseStack, faceBuffer, light, overlay);
        this.rightSide.render(poseStack, faceBuffer, light, overlay);
        this.backSide.render(poseStack, faceBuffer, light, overlay);
        this.frontSide.render(poseStack, faceBuffer, light, overlay);

        renderSide(data, SpriteFacing.FRONT, this.frontSide, poseStack, buffer, light, overlay);
        renderSide(data, SpriteFacing.BACK, this.backSide, poseStack, buffer, light, overlay);
        renderSide(data, SpriteFacing.LEFT, this.leftSide, poseStack, buffer, light, overlay);
        renderSide(data, SpriteFacing.RIGHT, this.rightSide, poseStack, buffer, light, overlay);
    }

    private void renderSide(CombinedPotData data, SpriteFacing facing, ModelPart part, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Optional<Item> sherd = switch (facing) {
            case FRONT -> data.decorations().front();
            case BACK -> data.decorations().back();
            case LEFT -> data.decorations().left();
            case RIGHT -> data.decorations().right();
        };
        boolean glowing = data.glow().get(facing.ordinal);
        Material material = overlayFor(data, facing, sherd, glowing);
        if (material != null) {
            part.render(poseStack, material.buffer(buffer, RenderType::entityTranslucent), glowing ? GlobalVariables.GLOW_STRENGTH : light, overlay);
        }
    }

    /** What goes over the face: the coloured engraving, or a brick pot's sherd art, or a sherd outline for other faces when there is art for it. */
    private static Material overlayFor(CombinedPotData data, SpriteFacing facing, Optional<Item> sherd, boolean glowing) {
        String sherdName = PotMaps.getSherdTexture(data.decorations()).get(facing.ordinal);
        Item engraved = data.engraving().getDecoration(facing);
        if (engraved != Items.AIR) {
            Material material = existing(new Material(CUSTOM_POT, NekomasFixed.id("trims/entity/pot/" + sherdName + "_" + PotMaps.ITEM_TO_STRING_MAPPING.get(engraved))));
            if (material != null && !"blank".equals(sherdName)) return material;
        }
        if (data.face().getSafeBlock() == Blocks.BRICKS) {
            if (sherd.isEmpty()) return null;
            if (glowing) {
                ResourceKey<DecoratedPotPattern> pattern = GlowingDecoratedPotPatternRegistry.fromSherd(sherd.get());
                Material material = pattern != null ? Sheets.getDecoratedPotMaterial(pattern) : null;
                if (material != null) return material;
            }
            return Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getPatternFromItem(sherd.get()));
        }
        return existing(new Material(TERRACOTTA_POT, NekomasFixed.id("entity/terracotta_decorated_pot/terracotta_decorated_pot_blank_" + sherdName + "_pottery_pattern")));
    }

    private static Material existing(Material material) {
        return material.sprite().contents().name().equals(MissingTextureAtlasSprite.getLocation()) ? null : material;
    }

    private static Material terracottaPot(String face, PotMaps.SpritePart part) {
        return new Material(TERRACOTTA_POT, NekomasFixed.id("entity/terracotta_decorated_pot/terracotta_decorated_pot_" + face + part));
    }

    @Override
    public AABB getRenderBoundingBox(TerracottaDecoratedPotBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.3, pos.getZ() + 1.0);
    }
}
