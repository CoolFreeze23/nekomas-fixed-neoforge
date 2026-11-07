package net.greenjab.nekomasfixed.registry.item;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

/** The Wildfire's smithing template. Explosions leave it alone, so it survives the fight that drops it. */
public class NetherHeartItem extends SmithingTemplateItem {
    public NetherHeartItem() {
        super(
                Component.translatable(
                        Util.makeDescriptionId("item", NekomasFixed.id("smithing_template.ingredients_shield_trident"))
                ).withStyle(ChatFormatting.BLUE),
                Component.translatable(
                        Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.ingredients"))
                ).withStyle(ChatFormatting.BLUE),
                Component.translatable(
                        Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template"))
                ).withStyle(ChatFormatting.GRAY),
                Component.nullToEmpty(""),
                Component.nullToEmpty(""),
                List.of(NekomasFixed.id("item/empty_slot_trident"), NekomasFixed.id("item/empty_slot_shield")),
                List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_ingot"))
        );
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !source.is(DamageTypeTags.IS_EXPLOSION);
    }
}
