package net.greenjab.nekomasfixed.registry.item;

import net.greenjab.nekomasfixed.util.FlowerCrownVariants;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jspecify.annotations.NonNull;

public class FlowerCrownItem extends Item implements Equipable {

    public final FlowerCrownVariants variant;

    public FlowerCrownItem(Properties properties, FlowerCrownVariants variant) {
        super(properties);
        this.variant = variant;
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    public FlowerCrownVariants getVariant() {
        return this.variant;
    }

    @Override
    public @NonNull EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public @NonNull Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }
}
