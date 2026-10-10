package com.atsuishio.portlib.forge.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

final class ClientReceivers {
    private ClientReceivers() {
    }

    static void register(SimpleChannel.Message<?> message) {
        ClientPlayNetworking.registerGlobalReceiver(message.type, (payload, context) -> {
            NetworkEvent.Context ctx = new NetworkEvent.Context(NetworkDirection.PLAY_TO_CLIENT, null, context.client());
            message.handle(payload.message, ctx);
        });
    }

    static void sendToServer(SimpleChannel.Payload payload) {
        if (Minecraft.getInstance().getConnection() != null) {
            ClientPlayNetworking.send(payload);
        }
    }
}
