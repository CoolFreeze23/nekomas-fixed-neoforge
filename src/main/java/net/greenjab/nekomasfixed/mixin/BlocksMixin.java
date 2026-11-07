package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.block.MelonBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Blocks.class)
public class BlocksMixin {
    // NeoForge refuses a second registration under one key, so minecraft:melon is built as a
    // MelonBlock in the first place instead of being replaced afterwards.
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;",
                    ordinal = 0
            ),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=melon"),
                    to = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/world/level/block/Blocks;MELON:Lnet/minecraft/world/level/block/Block;",
                            opcode = Opcodes.PUTSTATIC
                    )
            )
    )
    private static Block registerMelon(BlockBehaviour.Properties properties) {
        return new MelonBlock(false, properties);
    }
}
