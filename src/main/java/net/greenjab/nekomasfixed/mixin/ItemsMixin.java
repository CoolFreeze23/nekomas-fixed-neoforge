package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.registries.BlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Items.class)
public class ItemsMixin {

    // Builds minecraft:clock as a standing+wall block item in the first place. NeoForge refuses a
    // second registration under one key, so it can't be replaced afterwards.
    @Redirect(
            method = "<clinit>",
            at = @At(value = "NEW",
                    target = "(Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;",
                    ordinal = 0),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=clock"),
                    to = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/item/Items;CLOCK:Lnet/minecraft/world/item/Item;",
                            opcode = Opcodes.PUTSTATIC)))
    private static Item wallFloorClock(Item.Properties properties) {
        return new StandingAndWallBlockItem(BlockRegistry.CLOCK, BlockRegistry.WALL_CLOCK, properties, Direction.DOWN);
    }
}
