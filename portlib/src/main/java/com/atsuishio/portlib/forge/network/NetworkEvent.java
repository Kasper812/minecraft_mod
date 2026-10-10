package com.atsuishio.portlib.forge.network;

import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class NetworkEvent {
    private NetworkEvent() {
    }

    public static final class Context {
        private final NetworkDirection direction;
        private final @Nullable ServerPlayer sender;
        private final Executor executor;
        private boolean handled;

        public Context(NetworkDirection direction, @Nullable ServerPlayer sender, Executor executor) {
            this.direction = direction;
            this.sender = sender;
            this.executor = executor;
        }

        public CompletableFuture<Void> enqueueWork(Runnable runnable) {
            return CompletableFuture.runAsync(runnable, executor);
        }

        public @Nullable ServerPlayer getSender() {
            return sender;
        }

        public NetworkDirection getDirection() {
            return direction;
        }

        public void setPacketHandled(boolean handled) {
            this.handled = handled;
        }

        public boolean getPacketHandled() {
            return handled;
        }
    }
}
