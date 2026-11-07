package net.greenjab.nekomasfixed.network;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class SyncHandler {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(UpdateClockPayload.PACKET_ID, UpdateClockPayload.PACKET_CODEC, (payload, context) -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientPayloads.updateClock(payload, context);
            }
        });
    }

    /** Separate holder so that dedicated servers never resolve client classes. */
    private static final class ClientPayloads {
        static void updateClock(UpdateClockPayload payload, IPayloadContext context) {
            net.greenjab.nekomasfixed.ClientSyncHandler.updateClock(payload, context);
        }
    }
}
