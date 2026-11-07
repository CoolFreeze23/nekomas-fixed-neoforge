package net.greenjab.nekomasfixed.registry.item;

import net.greenjab.nekomasfixed.util.GoatHornHelmetTiers;
import net.minecraft.world.item.ArmorItem;

/** Stats, repairs and trims come from the armour material; GoatHornHelmetLayer draws it instead of the armour layer. */
public class GoatHornHelmetItem extends ArmorItem {

    private final GoatHornHelmetTiers tier;

    public GoatHornHelmetItem(Properties properties, GoatHornHelmetTiers tier) {
        super(tier.getMaterial(), Type.HELMET, properties);
        this.tier = tier;
    }

    public GoatHornHelmetTiers getTier() {
        return this.tier;
    }
}
