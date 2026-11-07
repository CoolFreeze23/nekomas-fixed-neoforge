package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.block.entity.TerracottaDecoratedPotBlockEntity;
import net.greenjab.nekomasfixed.registry.other.PotEngravingDecoration;
import net.greenjab.nekomasfixed.registry.other.PotFaceDecoration;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.DecoratedPotRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.PotDecorations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotRecipe.class)
public class DecoratedPotRecipeMixin {

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void customAssemble(CraftingInput input, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> cir) {
        PotDecorations decorations = new PotDecorations(input.getItem(1).getItem(), input.getItem(3).getItem(), input.getItem(5).getItem(), input.getItem(7).getItem());
        cir.setReturnValue(TerracottaDecoratedPotBlockEntity.createDecoratedPotInstance(decorations, PotEngravingDecoration.EMPTY,
                new PotFaceDecoration(Blocks.BRICKS.defaultBlockState())));
    }
}
