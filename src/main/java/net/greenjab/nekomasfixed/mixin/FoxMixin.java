package net.greenjab.nekomasfixed.mixin;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Fox.class)
public class FoxMixin {
    // Foxes use what they carry: hoes till, shears shear, flint and steel lights, potions apply, buckets fill.
    @Inject(method = "tick", at = @At("HEAD"))
    private void customTick(CallbackInfo ci) {
        Fox fox = (Fox) (Object) this;
        Level world = fox.level();
        if (world.isClientSide() || fox.getRandom().nextInt(10) != 5) return;
        ItemStack stack = fox.getMainHandItem();
        if (stack.is(ItemTags.HOES)) {
            if (world.getBlockState(fox.blockPosition().below()).is(BlockTags.DIRT)) {
                world.setBlockAndUpdate(fox.blockPosition().below(), Blocks.FARMLAND.defaultBlockState());
                stack.hurtAndBreak(1, fox, EquipmentSlot.MAINHAND);
            }
        } else if (stack.is(Items.SHEARS)) {
            List<Sheep> nearbySheep = world.getEntitiesOfClass(Sheep.class, fox.getBoundingBox().inflate(1), sheep -> true);
            if (!nearbySheep.isEmpty()) {
                Sheep sheep = nearbySheep.getFirst();
                if (sheep.readyForShearing()) {
                    sheep.shear(SoundSource.PLAYERS);
                    sheep.gameEvent(GameEvent.SHEAR, fox);
                    stack.hurtAndBreak(1, fox, EquipmentSlot.MAINHAND);
                }
            }
        } else if (stack.is(Items.FLINT_AND_STEEL)) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(fox.getRandom());
            boolean soul = world.getBlockState(fox.blockPosition().below()).is(Blocks.SOUL_SOIL);
            world.setBlockAndUpdate(fox.blockPosition().relative(dir), (soul ? Blocks.SOUL_FIRE : Blocks.FIRE).defaultBlockState());
            stack.hurtAndBreak(1, fox, EquipmentSlot.MAINHAND);
        } else if (stack.is(Items.POTION)) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null || !contents.hasEffects()) return;
            for (MobEffectInstance effect : contents.getAllEffects()) {
                fox.addEffect(new MobEffectInstance(effect));
            }
            fox.setItemInHand(InteractionHand.MAIN_HAND, Items.GLASS_BOTTLE.getDefaultInstance());
        } else if (stack.is(Items.BUCKET)) {
            var target = fox.blockPosition().below().relative(fox.getNearestViewDirection());
            if (world.getFluidState(target).is(Fluids.WATER)) {
                world.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
                fox.setItemInHand(InteractionHand.MAIN_HAND, Items.WATER_BUCKET.getDefaultInstance());
            } else if (world.getFluidState(target).is(Fluids.LAVA)) {
                world.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
                fox.setItemInHand(InteractionHand.MAIN_HAND, Items.LAVA_BUCKET.getDefaultInstance());
            }
        }
    }
}
