package net.greenjab.nekomasfixed.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.greenjab.nekomasfixed.registry.registries.ItemRegistry;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    // A slingshot draws in half the time of a bow, so its first-person pull is twice as fast.
    @ModifyConstant(method = "renderArmWithItem", constant = @Constant(floatValue = 20.0F))
    private float slingshotFasterPullTime(float constant, @Local(argsOnly = true) ItemStack stack) {
        return stack.is(ItemRegistry.SLINGSHOT) ? constant / 2 : constant;
    }
}
