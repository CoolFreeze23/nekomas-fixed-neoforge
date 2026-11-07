package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.util.options.EntitySpawnOption;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnEggItem.class)
public class SpawnOptionEggMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void refuseDisabledEntity(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getLevel() instanceof ServerLevel) {
            SpawnEggItem spawnEgg = (SpawnEggItem) (Object) this;
            if (EntitySpawnOption.isDisabled(spawnEgg.getType(context.getItemInHand()))) {
                Player player = context.getPlayer();
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("failed.entity_spawn").withStyle(ChatFormatting.RED));
                }
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
