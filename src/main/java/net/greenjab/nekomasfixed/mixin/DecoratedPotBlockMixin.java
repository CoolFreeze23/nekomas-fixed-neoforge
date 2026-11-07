package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.block.entity.PotMaps;
import net.greenjab.nekomasfixed.registry.block.enums.SpriteFacing;
import net.greenjab.nekomasfixed.registry.registries.ComponentRegistry;
import net.greenjab.nekomasfixed.target_access_class.DecoratedPotAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.PotDecorations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DecoratedPotBlock.class)
public class DecoratedPotBlockMixin {

    @Inject(
            method = "appendHoverText",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;of([Ljava/lang/Object;)Ljava/util/stream/Stream;"
            ),
            cancellable = true
    )
    private void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag, CallbackInfo ci) {
        PotDecorations decorations = stack.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY);
        List<Boolean> glowOverrides = stack.getOrDefault(ComponentRegistry.SHERD_GLOW_OVERRIDES, ComponentRegistry.DEFAULT_SHERD_GLOW_OVERRIDES);
        tooltipComponents.add(CommonComponents.EMPTY);
        tooltipComponents.add(PotMaps.sideTooltip(decorations.front(), Items.AIR, SpriteFacing.BACK, glowOverrides.get(DecoratedPotAccess.FRONT)));
        tooltipComponents.add(PotMaps.sideTooltip(decorations.left(), Items.AIR, SpriteFacing.LEFT, glowOverrides.get(DecoratedPotAccess.LEFT)));
        tooltipComponents.add(PotMaps.sideTooltip(decorations.right(), Items.AIR, SpriteFacing.RIGHT, glowOverrides.get(DecoratedPotAccess.RIGHT)));
        tooltipComponents.add(PotMaps.sideTooltip(decorations.back(), Items.AIR, SpriteFacing.FRONT, glowOverrides.get(DecoratedPotAccess.BACK)));
        ci.cancel();
    }
}
