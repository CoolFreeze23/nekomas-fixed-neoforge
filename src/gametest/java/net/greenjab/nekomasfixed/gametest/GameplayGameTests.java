package net.greenjab.nekomasfixed.gametest;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.registries.BlockRegistry;
import net.greenjab.nekomasfixed.registry.registries.EntityTypeRegistry;
import net.greenjab.nekomasfixed.registry.registries.VillagerRegistry;
import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.greenjab.nekomasfixed.target_access_class.DolphinAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NekomasFixed.NAMESPACE)
@PrefixGameTestTemplate(false)
public class GameplayGameTests {

    @GameTest(template = "platform")
    public static void pyrotechnicsTableIsAJobSite(GameTestHelper h) {
        var poi = PoiTypes.forState(BlockRegistry.PYROTECHNICS_TABLE.defaultBlockState());
        h.assertTrue(poi.isPresent() && poi.get().is(VillagerRegistry.PYROTECHNIST_POI_KEY), "table should be the pyrotechnist's job site");
        h.succeed();
    }

    @GameTest(template = "platform")
    public static void pyrotechnistHasTradesAtEveryLevel(GameTestHelper h) {
        var trades = VillagerTrades.TRADES.get(VillagerRegistry.PYROTECHNIST);
        h.assertTrue(trades != null, "no trades registered");
        for (int level = 1; level <= 5; level++) {
            VillagerTrades.ItemListing[] listings = trades.get(level);
            h.assertTrue(listings != null && listings.length >= 4, "too few trades at level " + level);
            var villager = EntityType.VILLAGER.create(h.getLevel());
            for (VillagerTrades.ItemListing listing : listings) {
                h.assertTrue(listing.getOffer(villager, h.getLevel().getRandom()) != null, "a trade gave nothing at level " + level);
            }
            villager.discard();
        }
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 600)
    public static void zombieInPowderSnowBecomesRime(GameTestHelper h) {
        BlockPos pos = new BlockPos(2, 2, 2);
        h.setBlock(pos, Blocks.POWDER_SNOW);
        h.setBlock(pos.above(), Blocks.POWDER_SNOW);
        Zombie zombie = h.spawn(EntityType.ZOMBIE, pos);
        zombie.setNoAi(true);
        h.succeedWhen(() -> h.assertEntityPresent(EntityTypeRegistry.RIME));
    }

    @GameTest(template = "platform")
    public static void dolphinRemembersTropicalFish(GameTestHelper h) {
        Dolphin dolphin = h.spawn(EntityType.DOLPHIN, new BlockPos(2, 2, 2));
        h.assertTrue(!((DolphinAccess) dolphin).nekomasfixed$isTropicalFishFed(), "starts unfed");
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TROPICAL_FISH));
        dolphin.mobInteract(player, InteractionHand.MAIN_HAND);
        h.assertTrue(((DolphinAccess) dolphin).nekomasfixed$isTropicalFishFed(), "fish should be remembered");
        h.succeed();
    }

    @GameTest(template = "platform", timeoutTicks = 100)
    public static void chainmailHelmetSievesFallingGravel(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
        BlockPos pos = new BlockPos(2, 2, 2);
        player.moveTo(h.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(pos)));
        h.getLevel().addFreshEntity(player);
        FallingBlockEntity falling = FallingBlockEntity.fall(h.getLevel(), h.absolutePos(pos.above(1)), Blocks.GRAVEL.defaultBlockState());
        h.succeedWhen(() -> {
            h.assertTrue(falling.isRemoved(), "still falling");
            h.assertTrue(player.getItemBySlot(EquipmentSlot.HEAD).getDamageValue() > 0, "helmet should take the hit");
            player.discard();
        });
    }

    @GameTest(template = "platform")
    public static void disabledOptionStopsSpawns(GameTestHelper h) {
        ModConfigValues.spawnRime.set(false);
        try {
            var rime = EntityTypeRegistry.RIME.create(h.getLevel());
            rime.moveTo(h.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2, 2.5)));
            h.assertTrue(!h.getLevel().addFreshEntity(rime), "a disabled entity should not join the level");
        } finally {
            ModConfigValues.spawnRime.set(true);
        }
        h.succeed();
    }
}
