package com.atsuishio.portlib.forge.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SimpleChannel {
    static final List<SimpleChannel> CHANNELS = new ArrayList<>();

    private final Identifier name;
    final Map<Class<?>, Message<?>> byClass = new HashMap<>();
    final List<Message<?>> messages = new ArrayList<>();

    SimpleChannel(Identifier name) {
        this.name = name;
        CHANNELS.add(this);
    }

    public Identifier getName() {
        return name;
    }

    public <M> void registerMessage(int index, Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder,
                                    BiConsumer<M, Supplier<NetworkEvent.Context>> consumer) {
        registerMessage(index, type, encoder, decoder, consumer, Optional.empty());
    }

    public <M> void registerMessage(int index, Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder,
                                    BiConsumer<M, Supplier<NetworkEvent.Context>> consumer, Optional<NetworkDirection> direction) {
        String path = name.getPath() + "/" + index + "_" + type.getSimpleName().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        CustomPacketPayload.Type<Payload> payloadType = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(name.getNamespace(), path));
        Message<M> message = new Message<>(this, payloadType, type, encoder, decoder, consumer, direction.orElse(null));
        StreamCodec<RegistryFriendlyByteBuf, Payload> codec = StreamCodec.of(
                (buf, payload) -> message.encode(payload.message, buf),
                buf -> new Payload(payloadType, message.decoder.apply(buf)));
        boolean toServer = message.direction == null || message.direction == NetworkDirection.PLAY_TO_SERVER;
        boolean toClient = message.direction == null || message.direction == NetworkDirection.PLAY_TO_CLIENT;
        if (toServer) {
            PayloadTypeRegistry.serverboundPlay().register(payloadType, codec);
            ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, context) -> {
                ServerPlayer player = context.player();
                NetworkEvent.Context ctx = new NetworkEvent.Context(NetworkDirection.PLAY_TO_SERVER, player, context.server());
                message.handle(payload.message, ctx);
            });
        }
        if (toClient) {
            PayloadTypeRegistry.clientboundPlay().register(payloadType, codec);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientReceivers.register(message);
            }
        }
        byClass.put(type, message);
        messages.add(message);
    }

    @SuppressWarnings("unchecked")
    private <M> Message<M> lookup(M msg) {
        Message<M> message = (Message<M>) byClass.get(msg.getClass());
        if (message == null) {
            throw new IllegalArgumentException("Unregistered message " + msg.getClass().getName() + " on channel " + name);
        }
        return message;
    }

    public <M> void sendToServer(M msg) {
        ClientReceivers.sendToServer(new Payload(lookup(msg).type, msg));
    }

    public <M> void send(PacketDistributor.PacketTarget target, M msg) {
        Payload payload = new Payload(lookup(msg).type, msg);
        if (target.toServer) {
            ClientReceivers.sendToServer(payload);
            return;
        }
        for (ServerPlayer player : target.players.get()) {
            if (ServerPlayNetworking.canSend(player, payload.type())) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    public <M> void sendTo(M msg, ServerPlayer player) {
        send(PacketDistributor.PLAYER.with(player), msg);
    }

    public static final class Payload implements CustomPacketPayload {
        private final Type<Payload> type;
        final Object message;

        Payload(Type<Payload> type, Object message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return type;
        }
    }

    static final class Message<M> {
        final SimpleChannel channel;
        final CustomPacketPayload.Type<Payload> type;
        final Class<M> cls;
        final BiConsumer<M, FriendlyByteBuf> encoder;
        final Function<FriendlyByteBuf, M> decoder;
        final BiConsumer<M, Supplier<NetworkEvent.Context>> consumer;
        final NetworkDirection direction;

        Message(SimpleChannel channel, CustomPacketPayload.Type<Payload> type, Class<M> cls, BiConsumer<M, FriendlyByteBuf> encoder,
                Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<NetworkEvent.Context>> consumer, NetworkDirection direction) {
            this.channel = channel;
            this.type = type;
            this.cls = cls;
            this.encoder = encoder;
            this.decoder = decoder;
            this.consumer = consumer;
            this.direction = direction;
        }

        @SuppressWarnings("unchecked")
        void encode(Object msg, FriendlyByteBuf buf) {
            encoder.accept((M) msg, buf);
        }

        @SuppressWarnings("unchecked")
        void handle(Object msg, NetworkEvent.Context ctx) {
            consumer.accept((M) msg, () -> ctx);
        }
    }
}
