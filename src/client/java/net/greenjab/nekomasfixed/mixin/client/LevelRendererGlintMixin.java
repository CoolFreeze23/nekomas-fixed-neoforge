package net.greenjab.nekomasfixed.mixin.client;

import net.greenjab.nekomasfixed.render.other.TintedGlint;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererGlintMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;waterMask()Lnet/minecraft/client/renderer/RenderType;"))
    private void nekomasfixed$flushTintedFoil(CallbackInfo ci) {
        TintedGlint.flush();
    }
}
