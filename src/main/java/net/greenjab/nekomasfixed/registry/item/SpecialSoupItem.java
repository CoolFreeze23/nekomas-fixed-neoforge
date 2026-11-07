package net.greenjab.nekomasfixed.registry.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

public class SpecialSoupItem extends Item {

    public SpecialSoupItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!level.isClientSide() && user instanceof Player player) {
            ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            for (ItemStack ingredient : contents.nonEmptyItemsCopy()) {
                PotionContents potions = ingredient.get(DataComponents.POTION_CONTENTS);
                if (potions != null) {
                    // Half as long as drinking it.
                    potions.getAllEffects().forEach(effect -> player.addEffect(new MobEffectInstance(effect.getEffect(),
                            effect.isInfiniteDuration() ? effect.getDuration() : effect.getDuration() / 2, effect.getAmplifier(), effect.isAmbient(), effect.isVisible())));
                    continue;
                }
                // Whatever else eating it does (a golden apple's effects, chorus teleporting, milk clearing effects), but only half the food.
                FoodProperties food = ingredient.get(DataComponents.FOOD);
                FoodData foodData = player.getFoodData();
                int foodLevel = foodData.getFoodLevel();
                float saturation = foodData.getSaturationLevel();
                ingredient.finishUsingItem(level, player);
                foodData.setFoodLevel(foodLevel);
                foodData.setSaturation(saturation);
                if (food != null) foodData.eat(Mth.ceil(food.nutrition() / 2f), food.saturation() / 2f);
            }
        }
        return super.finishUsingItem(stack, level, user);
    }
}
