package net.greenjab.nekomasfixed.registry.item;

import net.greenjab.nekomasfixed.registry.entity.BigBoat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Predicate;

public class BigBoatItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final EntityType<? extends BigBoat> type;

    public BigBoatItem(EntityType<? extends BigBoat> type, Item.Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(@NonNull Level level, Player player, @NonNull InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }
        Vec3 view = player.getViewVector(1.0F);
        List<Entity> entities = level.getEntities(player,
                player.getBoundingBox().expandTowards(view.scale(5.0D)).inflate(1.0D), ENTITY_PREDICATE);
        Vec3 eye = player.getEyePosition();
        for (Entity entity : entities) {
            if (entity.getBoundingBox().inflate(entity.getPickRadius()).contains(eye)) {
                return InteractionResultHolder.pass(itemStack);
            }
        }
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BigBoat boat = this.type.create(level);
            if (boat == null) {
                return InteractionResultHolder.fail(itemStack);
            }
            Vec3 at = hitResult.getLocation();
            boat.setPos(at.x, at.y, at.z);
            boat.xo = at.x;
            boat.yo = at.y;
            boat.zo = at.z;
            if (level instanceof ServerLevel serverLevel) {
                EntityType.<BigBoat>createDefaultStackConfig(serverLevel, itemStack, player).accept(boat);
            }
            boat.setYRot(player.getYRot());
            if (!level.noCollision(boat, boat.getBoundingBox())) {
                return InteractionResultHolder.fail(itemStack);
            }
            if (!level.isClientSide) {
                level.addFreshEntity(boat);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
                itemStack.consume(1, player);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }
        return InteractionResultHolder.pass(itemStack);
    }
}
