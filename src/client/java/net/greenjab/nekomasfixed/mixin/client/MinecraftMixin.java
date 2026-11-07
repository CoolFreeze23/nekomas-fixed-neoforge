package net.greenjab.nekomasfixed.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.greenjab.nekomasfixed.util.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    // Dual-wielding sickles: the main hand passes, so right-clicking an entity reaches the off-hand attack.
    @WrapOperation(method = "startUseItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult allowOffhandAttack(MultiPlayerGameMode instance, Player player, Entity entity, InteractionHand hand, Operation<InteractionResult> original) {
        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(InteractionHand.MAIN_HAND).is(ModTags.SICKLES)
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModTags.SICKLES)) {
            return InteractionResult.PASS;
        }
        return original.call(instance, player, entity, hand);
    }
}
