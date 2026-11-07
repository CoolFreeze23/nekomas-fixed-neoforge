package net.greenjab.nekomasfixed.registry.item;

import java.util.List;

import net.greenjab.nekomasfixed.registry.entity.WildfireTrident;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class WildfireTridentItem extends Item implements ProjectileItem {

    public WildfireTridentItem(Item.Properties settings) {
        super(settings);
    }

    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.0F, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9F, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolComponent() {
        return new Tool(List.of(), 1.0F, 2);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (user instanceof Player playerEntity) {
            int i = this.getUseDuration(stack, user) - remainingUseTicks;
            if (i < 10) return;
            float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity);
            if (f > 0.0F && !playerEntity.isOnFire() && !playerEntity.isInWaterOrRain()) return;
            if (isTooDamagedToUse(stack)) return;
            Holder<SoundEvent> registryEntry = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
            playerEntity.awardStat(Stats.ITEM_USED.get(this));
            if (!level.isClientSide()) {
                stack.hurtAndBreak(1, playerEntity, LivingEntity.getSlotForHand(user.getUsedItemHand()));
                if (f == 0.0F) {
                    WildfireTrident tridentEntity = new WildfireTrident(level, playerEntity, stack);
                    tridentEntity.shootFromRotation(playerEntity, playerEntity.getXRot(), playerEntity.getYRot(), 0.0F, 2.5F, 1.0F);
                    if (playerEntity.hasInfiniteMaterials()) tridentEntity.pickup = Pickup.CREATIVE_ONLY;
                    level.addFreshEntity(tridentEntity);
                    level.playSound(null, tridentEntity, registryEntry.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (!playerEntity.hasInfiniteMaterials()) playerEntity.getInventory().removeItem(stack);
                    return;
                }
            }

            if (f > 0.0F) {
                float g = playerEntity.getYRot();
                float h = playerEntity.getXRot();
                float j = -Mth.sin((g * ((float)Math.PI / 180F))) * Mth.cos((h * ((float)Math.PI / 180F)));
                float k = -Mth.sin((h * ((float)Math.PI / 180F)));
                float l = Mth.cos((g * ((float)Math.PI / 180F))) * Mth.cos((h * ((float)Math.PI / 180F)));
                float m = Mth.sqrt(j * j + k * k + l * l);
                j *= f / m;
                k *= f / m;
                l *= f / m;
                playerEntity.push(j, k, l);
                playerEntity.startAutoSpinAttack(20, 8.0F, stack);
                if (playerEntity.onGround())  playerEntity.move(MoverType.SELF, new Vec3(0.0F, 1.1999999F, 0.0F));
                level.playSound(null, playerEntity, registryEntry.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (isTooDamagedToUse(itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        } else if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, user) > 0.0F && !user.isOnFire() && !user.isInWaterOrRain()) {
            return InteractionResultHolder.fail(itemStack);
        } else {
            user.startUsingItem(hand);
            return InteractionResultHolder.consume(itemStack);
        }
    }

    private static boolean isTooDamagedToUse(ItemStack stack) {
        return stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        WildfireTrident tridentEntity = new WildfireTrident(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        tridentEntity.pickup = Pickup.ALLOWED;
        return tridentEntity;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_TRIDENT_ACTIONS.contains(itemAbility);
    }
}
