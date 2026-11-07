package net.greenjab.nekomasfixed.util;

import net.greenjab.nekomasfixed.registry.item.quiver.QuiverContents;
import net.greenjab.nekomasfixed.registry.registries.ArcheryRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Item model property: 1 while the quiver holds no arrows. */
public class QuiverComponentChangeProperty implements ClampedItemPropertyFunction {
    public static QuiverListState state(ItemStack stack) {
        QuiverContents content = stack.get(ArcheryRegistry.QUIVER_CONTENTS);
        return content == null || content.isEmpty() ? QuiverListState.EMPTY : QuiverListState.NOT_EMPTY;
    }

    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed) {
        return state(stack) == QuiverListState.EMPTY ? 1.0F : 0.0F;
    }
}
