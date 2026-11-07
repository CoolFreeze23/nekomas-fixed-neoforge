/*
 * The glint texture tinting in build() is adapted from Glint Everything(26.2)
 * (https://github.com/dev-limucc/glint-everything) by Limucc-dev, used under the MIT License:
 *
 * Copyright (c) 2026 Limucc-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.greenjab.nekomasfixed.render.other;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.greenjab.nekomasfixed.mixin.client.BufferSourceAccessor;
import net.greenjab.nekomasfixed.util.GlintColors;
import net.greenjab.nekomasfixed.util.GlintContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coloured foil. Each colour is a tinted copy of the glint texture with its own render type, using the
 * vanilla glint shaders. The render types get fixed buffers in the game's buffer source, because a foil
 * buffer stays open while the item's own buffer is written, and they must draw after the item they cover.
 */
public final class TintedGlint {
    private static final Logger LOGGER = LoggerFactory.getLogger("nekomasfixed/TintedGlint");

    public enum Kind {
        ITEM(RenderStateShard.RENDERTYPE_GLINT_SHADER, false),
        ITEM_TRANSLUCENT(RenderStateShard.RENDERTYPE_GLINT_TRANSLUCENT_SHADER, false),
        ENTITY(RenderStateShard.RENDERTYPE_ENTITY_GLINT_SHADER, true),
        ENTITY_DIRECT(RenderStateShard.RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER, true),
        ARMOR(RenderStateShard.RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER, true);

        private final RenderStateShard.ShaderStateShard shader;
        private final boolean entityTexture;

        Kind(RenderStateShard.ShaderStateShard shader, boolean entityTexture) {
            this.shader = shader;
            this.entityTexture = entityTexture;
        }
    }

    private record TexKey(boolean entity, int color, ResourceLocation base) {}
    private static final Map<TexKey, ResourceLocation> TEXTURES = new HashMap<>();

    private record TypeKey(Kind kind, int color, ResourceLocation base) {}
    private static final Map<TypeKey, RenderType> TYPES = new HashMap<>();
    private static final List<RenderType> FLUSH_ORDER = new ArrayList<>();

    // Gamma
    private static final double GAMMA = 0.75;
    // Glint Strength
    private static final float GAIN = 1.2f;

    private TintedGlint() {}

    private static String baseTag(ResourceLocation base) {
        return base == null ? "" : "_" + Integer.toHexString(base.hashCode());
    }

    /**
     * Armor layers draw without the view offset of vanilla's armor render types, so armor foil has to match
     * them or it fails its depth test. A colour of 0 gives the plain foil here.
     */
    public static RenderType armor(MultiBufferSource buffer, RenderType vanilla) {
        if (buffer instanceof MultiBufferSource.BufferSource source && source != Minecraft.getInstance().renderBuffers().bufferSource()) {
            return vanilla;
        }
        RenderType type = type(Kind.ARMOR, GlintContext.getColor(), GlintContext.getBase());
        return type != null ? type : vanilla;
    }

    /** The tinted version of a vanilla foil render type for the item being drawn, or the same type when it has no glint colour. */
    public static RenderType replace(RenderType vanilla, MultiBufferSource buffer) {
        int color = GlintContext.getColor();
        if (color == 0) {
            return vanilla;
        }
        // Another buffer source has no fixed buffers for these types.
        if (buffer instanceof MultiBufferSource.BufferSource source && source != Minecraft.getInstance().renderBuffers().bufferSource()) {
            return vanilla;
        }
        Kind kind;
        if (vanilla == RenderType.glint()) {
            kind = Kind.ITEM;
        } else if (vanilla == RenderType.glintTranslucent()) {
            kind = Kind.ITEM_TRANSLUCENT;
        } else if (vanilla == RenderType.entityGlint()) {
            kind = Kind.ENTITY;
        } else {
            kind = Kind.ENTITY_DIRECT;
        }
        RenderType tinted = type(kind, color, GlintContext.getBase());
        return tinted != null ? tinted : vanilla;
    }

    public static @Nullable RenderType type(Kind kind, int color, @Nullable ResourceLocation base) {
        ResourceLocation glintTexture = texture(kind.entityTexture, color, base);
        if (glintTexture == null) return null;

        TypeKey key = new TypeKey(kind, color, base);
        RenderType cached = TYPES.get(key);
        if (cached != null) return cached;

        RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder()
                .setShaderState(kind.shader)
                .setTextureState(new RenderStateShard.TextureStateShard(glintTexture, true, false))
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setCullState(RenderStateShard.NO_CULL)
                .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
                .setTexturingState(kind.entityTexture ? RenderStateShard.ENTITY_GLINT_TEXTURING : RenderStateShard.GLINT_TEXTURING);
        if (kind == Kind.ITEM_TRANSLUCENT || kind == Kind.ENTITY) {
            builder.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET);
        }

        RenderType type = RenderType.create(
                "nekomasfixed_glint_" + kind.name().toLowerCase() + "_" + Integer.toHexString(color) + baseTag(base),
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 1536, builder.createCompositeState(false));

        TYPES.put(key, type);
        FLUSH_ORDER.add(type);
        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        ((BufferSourceAccessor) source).nekomasfixed$getFixedBuffers().put(type, new ByteBufferBuilder(type.bufferSize()));
        return type;
    }

    /** Draws the tinted foil at the point where the world pass draws the vanilla foil. */
    public static void flush() {
        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        for (RenderType type : FLUSH_ORDER) {
            source.endBatch(type);
        }
    }

    private static @Nullable ResourceLocation texture(boolean entity, int color, @Nullable ResourceLocation base) {
        if (color == 0) {
            return entity ? ItemRenderer.ENCHANTED_GLINT_ENTITY : ItemRenderer.ENCHANTED_GLINT_ITEM;
        }
        TexKey key = new TexKey(entity, color, base);
        if (TEXTURES.containsKey(key)) {
            return TEXTURES.get(key);
        }

        ResourceLocation result = build(entity, color, base);
        TEXTURES.put(key, result);
        return result;
    }

    private static @Nullable ResourceLocation build(boolean entity, int color, @Nullable ResourceLocation base) {
        Minecraft mc = Minecraft.getInstance();

        int tint = GlintColors.lift(color, GlintColors.GLINT_FLOOR, GlintColors.GLINT_FLOOR_GRAY);
        int tintA = tint >>> 24 & 0xFF;
        int tintR = tint >> 16 & 0xFF;
        int tintG = tint >> 8 & 0xFF;
        int tintB = tint & 0xFF;

        ResourceLocation source = base != null ? base : (entity ? ItemRenderer.ENCHANTED_GLINT_ENTITY : ItemRenderer.ENCHANTED_GLINT_ITEM);

        try (InputStream in = mc.getResourceManager().getResourceOrThrow(source).open()) {
            NativeImage image = NativeImage.read(in);

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    // NativeImage pixels are ABGR.
                    int pixel = image.getPixelRGBA(x, y);
                    int rawBrightness = Math.max(pixel >> 16 & 0xFF, Math.max(pixel >> 8 & 0xFF, pixel & 0xFF));
                    int brightness = Math.min(255, Math.round((float) Math.pow(rawBrightness / 255.0, GAMMA) * 255f * GAIN));

                    int a = (pixel >>> 24 & 0xFF) * tintA / 255;
                    int r = brightness * tintR / 255;
                    int g = brightness * tintG / 255;
                    int b = brightness * tintB / 255;

                    image.setPixelRGBA(x, y, a << 24 | b << 16 | g << 8 | r);
                }
            }

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("nekomasfixed",
                    "tinted_glint/" + (entity ? "entity_" : "item_") + Integer.toHexString(color) + baseTag(base));
            mc.getTextureManager().register(id, new DynamicTexture(image));
            return id;
        } catch (Exception e) {
            LOGGER.warn("Could not build tinted glint texture for color {} (base {})", Integer.toHexString(color), base, e);
            return null;
        }
    }

    /** Resource reload: textures rebuild on next use. The render types keep their ids and stay valid. */
    public static void clearCache() {
        Minecraft mc = Minecraft.getInstance();
        for (ResourceLocation id : TEXTURES.values()) {
            if (id != null) {
                mc.getTextureManager().release(id);
            }
        }
        TEXTURES.clear();
    }
}
