package net.greenjab.nekomasfixed;

import net.greenjab.nekomasfixed.network.UpdateClockPayload;
import net.greenjab.nekomasfixed.registry.block.entity.ClockBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientSyncHandler {

    public static void updateClock(UpdateClockPayload payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null
                && client.level.getBlockEntity(new BlockPos(payload.x(), payload.y(), payload.z())) instanceof ClockBlockEntity clockBlockEntity) {
            clockBlockEntity.setTimer(payload.timer());
            clockBlockEntity.setBell(payload.hasBell());
            clockBlockEntity.setShowsTime(payload.showsTime());
        }
    }
}
