package net.greenjab.nekomasfixed.registry.item;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

/** A helmet of a vanilla armour material worn with its own texture, so trims keep that material's palette. */
public class CrownItem extends ArmorItem {

    private final ResourceLocation texture;

    public CrownItem(Holder<ArmorMaterial> material, String name, Properties properties) {
        super(material, Type.HELMET, properties);
        this.texture = NekomasFixed.id("textures/models/armor/" + name + "_layer_1.png");
    }

    @Override
    public ResourceLocation getArmorTexture(@NonNull ItemStack stack, @NonNull Entity entity, @NonNull EquipmentSlot slot, ArmorMaterial.@NonNull Layer layer, boolean innerModel) {
        return this.texture;
    }
}
