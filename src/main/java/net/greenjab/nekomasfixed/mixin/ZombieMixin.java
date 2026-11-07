package net.greenjab.nekomasfixed.mixin;

import net.greenjab.nekomasfixed.registry.registries.EntityTypeRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster {
    @Unique private int inPowderSnowTime = 0;

    protected ZombieMixin(EntityType<? extends Monster> type, Level world) {
        super(type, world);
    }

    // A plain zombie that stays in powder snow turns into a Rime.
    @Inject(method = "tick", at = @At("TAIL"))
    private void tickRimeConversion(CallbackInfo ci) {
        if (this.level() instanceof ServerLevel level && this.isAlive() && this.isInPowderSnow && this.getType() == EntityType.ZOMBIE) {
            this.inPowderSnowTime++;
            if (this.inPowderSnowTime >= 450) {
                this.convertTo(EntityTypeRegistry.RIME, true);
                if (!this.isSilent()) level.levelEvent(null, LevelEvent.SOUND_SKELETON_TO_STRAY, this.blockPosition(), 0);
            }
        } else {
            this.inPowderSnowTime = 0;
        }
    }
}
