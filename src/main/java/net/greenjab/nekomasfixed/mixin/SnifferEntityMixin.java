package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.mixin.accessor.SnifferAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sniffer.class)
public class SnifferEntityMixin {

    @Inject(method = "dropSeed", at = @At("HEAD"), cancellable = true)
    private void dropCustomLoot(CallbackInfo ci) {
        Sniffer sniffer = (Sniffer) (Object) this;
        Level world = sniffer.level();
        if (world instanceof ServerLevel serverWorld && sniffer.getEntityData().get(SnifferAccessor.getFinishDigTime()) == sniffer.tickCount) {
            BlockPos blockPos = ((SnifferAccessor) sniffer).invokeGetDigPos();

            Biome biome = serverWorld.getBiome(blockPos).value();
            String biomeName = serverWorld.getBiome(blockPos).unwrapKey().map(key -> key.location().toString()).orElse("unknown");

            ResourceKey<LootTable> lootTableKey;
            if (biome.getBaseTemperature() <= 0.15f) {
                lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("gameplay/sniffer_digging_snowy"));
            } else if (biomeName.contains("des")) {
                lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("gameplay/sniffer_digging_desert"));
            } else if (biomeName.contains("badlands")) {
                lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("gameplay/sniffer_digging_badlands"));
            } else {
                lootTableKey = BuiltInLootTables.SNIFFER_DIGGING;
            }

            LootParams params = new LootParams.Builder(serverWorld)
                    .withParameter(LootContextParams.ORIGIN, ((SnifferAccessor) sniffer).invokeGetHeadPosition())
                    .withParameter(LootContextParams.THIS_ENTITY, sniffer)
                    .create(LootContextParamSets.GIFT);
            for (ItemStack itemStack : serverWorld.getServer().reloadableRegistries().getLootTable(lootTableKey).getRandomItems(params)) {
                ItemEntity itemEntity = new ItemEntity(serverWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
                itemEntity.setDefaultPickUpDelay();
                serverWorld.addFreshEntity(itemEntity);
            }

            sniffer.playSound(SoundEvents.SNIFFER_DROP_SEED, 1.0F, 1.0F);
            ci.cancel();
        }
    }
}
