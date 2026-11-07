package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.util.options.EntitySpawnOption;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getLevel() instanceof ServerLevel) {
            SpawnEggItem spawnEgg = (SpawnEggItem) (Object) this;
            EntityType<?> entity = spawnEgg.getType(context.getItemInHand());
            if (EntitySpawnOption.isDisabled(entity)) {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.translatable("failed.entity_spawn").withStyle(ChatFormatting.RED));
                }
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
