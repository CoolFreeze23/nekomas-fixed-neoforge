package net.greenjab.nekomasfixed.util;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;

public enum GoatHornHelmetTiers {
    IRON(ArmorMaterials.IRON, 15),
    GOLDEN(ArmorMaterials.GOLD, 7),
    CHAINMAIL(ArmorMaterials.CHAIN, 15),
    DIAMOND(ArmorMaterials.DIAMOND, 33),
    NETHERITE(ArmorMaterials.NETHERITE, 37),
    TURTLE(ArmorMaterials.TURTLE, 25);

    private final Holder<ArmorMaterial> material;
    private final int durability;
    private final ResourceLocation texture;

    GoatHornHelmetTiers(Holder<ArmorMaterial> material, int durability) {
        this.material = material;
        this.durability = durability;
        this.texture = ResourceLocation.fromNamespaceAndPath("nekomasfixed",
                "textures/entity/equipment/humanoid/goat_horn_helm/" + this.name().toLowerCase());
    }

    public Holder<ArmorMaterial> getMaterial() { return this.material; }
    public int getDurability() { return this.durability; }
    public ResourceLocation getTexture() { return this.texture; }
}
