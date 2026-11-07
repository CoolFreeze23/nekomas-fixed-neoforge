package net.greenjab.nekomasfixed.mixin.boat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.greenjab.nekomasfixed.registry.entity.BigBoat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatMixin {

    @Shadow private float deltaRotation;

    @Shadow private float invFriction;

    @Shadow protected abstract void controlBoat();

    @Shadow protected abstract int getMaxPassengers();

    @WrapOperation(method = "controlBoat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setYRot(F)V"))
    private void adjustTurningForBigBoat(Boat boat, float v, Operation<Void> original) {
        float f = 1.0F;
        if (boat instanceof BigBoat bigBoat) f = bigBoat.getRotationSpeed();
        original.call(boat, boat.getYRot() + deltaRotation * f);
    }

    @WrapOperation(method = "positionRider", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setYRot(F)V"))
    private void adjustTurningForBigBoat2(Entity passenger, float yRot, Operation<Void> original) {
        float f = 1.0F;
        Boat boat = (Boat)(Object)this;
        if (boat instanceof BigBoat bigBoat) f = bigBoat.getRotationSpeed();
        original.call(passenger, passenger.getYRot() + deltaRotation * f);
    }

    @WrapOperation(method = "positionRider", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setYHeadRot(F)V", ordinal = 0))
    private void adjustTurningForBigBoat3(Entity passenger, float yHeadRot, Operation<Void> original) {
        float f = 1.0F;
        Boat boat = (Boat)(Object)this;
        if (boat instanceof BigBoat bigBoat) f = bigBoat.getRotationSpeed();
        original.call(passenger, passenger.getYHeadRot() + deltaRotation * f);
    }

    @ModifyExpressionValue(method = "positionRider", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int animalsFaceSideways(int original) {
        if (original < 2) return original;
        return getMaxPassengers();
    }

    @Inject(method = "clampRotation", at = @At("HEAD"), cancellable = true)
    private void adjustTurningForBigBoat4(Entity passenger, CallbackInfo ci) {
        Boat boat = (Boat)(Object)this;
        if (!(passenger instanceof Player)) {
            passenger.setYBodyRot(boat.getYRot());
            float f = Mth.wrapDegrees(passenger.getYHeadRot() - boat.getYRot());
            float g = Mth.clamp(f, -105.0F, 105.0F) + boat.getYRot();
            passenger.yRotO += g;
            passenger.setYRot(g);
            passenger.setYHeadRot(g);
            ci.cancel();
        }
    }

    @WrapOperation(method = "controlBoat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void adjustAccelerationForBigBoat(Boat instance, Vec3 vec3, Operation<Void> original, @Local(ordinal = 0) float acceleration) {
        Boat boat = (Boat)(Object)this;
        if (boat instanceof BigBoat bigBoat) acceleration *= bigBoat.getSpeed();
        original.call(instance,
                boat.getDeltaMovement().add(Mth.sin(-boat.getYRot() * (float)(Math.PI / 180.0)) * acceleration, 0.0, Mth.cos(boat.getYRot() * (float)(Math.PI / 180.0)) * acceleration)
        );
    }

    @WrapOperation(method = "floatBoat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(DDD)V"))
    private void adjustSpeedForBigBoat2(Boat instance, double x, double y, double z, Operation<Void> original) {
        Boat boat = (Boat)(Object)this;
        float friction = this.invFriction;
        if (boat instanceof BigBoat bigBoat) {
            friction = 1.0F - (1.0F - friction) / (bigBoat.getSpeed() * 3.0F);
        }
        Vec3 movement = boat.getDeltaMovement();
        original.call(instance, movement.x * (double)friction, y, movement.z * (double)friction);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z", opcode = Opcodes.GETFIELD, ordinal = 1))
    private boolean letIllagerControl(boolean original) {
        if (!original) {
            Boat boat = (Boat)(Object)this;
            if (boat.getFirstPassenger() instanceof Raider) {
                this.controlBoat();
            }
        }
        return original;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void stopTurnWhenEmpty(CallbackInfo ci) {
        Boat boat = (Boat)(Object)this;
        if (!(boat.getFirstPassenger() instanceof Player || boat.getFirstPassenger() instanceof Raider)) {
            deltaRotation = 0.0F;
        }
    }
}
