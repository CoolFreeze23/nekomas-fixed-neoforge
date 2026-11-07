package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.block.FletchingTableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// minecraft:fletching_table is built as the mod's block in the first place: NeoForge refuses a second registration under one key.
@Mixin(Blocks.class)
public class BlocksFletchingMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/FletchingTableBlock;"))
    private static net.minecraft.world.level.block.FletchingTableBlock newFletchingTable(BlockBehaviour.Properties properties) {
        return new FletchingTableBlock(properties);
    }
}
