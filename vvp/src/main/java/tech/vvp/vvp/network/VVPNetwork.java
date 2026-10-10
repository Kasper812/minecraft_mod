package tech.vvp.vvp.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.atsuishio.portlib.forge.network.NetworkDirection;
import com.atsuishio.portlib.forge.network.NetworkEvent;
import com.atsuishio.portlib.forge.network.NetworkRegistry;
import com.atsuishio.portlib.forge.network.SimpleChannel;
import tech.vvp.vvp.network.message.PantsirLockRequestMessage;
import tech.vvp.vvp.network.message.PantsirRadarSyncMessage;
import tech.vvp.vvp.network.message.SeatSwapMessage;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.Optional;

public class VVPNetwork {
    
    public static final String MOD_ID = "vvp";
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel VVP_HANDLER = NetworkRegistry.newSimpleChannel(Identifier.fromNamespaceAndPath(MOD_ID, MOD_ID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        VVP_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
        messageID++;
    }

    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer, Optional<NetworkDirection> direction) {
        VVP_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer, direction);
        messageID++;
    }
    
    /**
     * Регистрация всех сетевых сообщений VVP
     */
    public static void register() {
        // Клиент -> Сервер: запрос захвата цели
        addNetworkMessage(
            PantsirLockRequestMessage.class,
            PantsirLockRequestMessage::encode,
            PantsirLockRequestMessage::decode,
            PantsirLockRequestMessage::handler,
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        
        // Сервер -> Клиент: синхронизация состояния радара
        addNetworkMessage(
            PantsirRadarSyncMessage.class,
            PantsirRadarSyncMessage::encode,
            PantsirRadarSyncMessage::decode,
            PantsirRadarSyncMessage::handler,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        
        // Клиент -> Сервер: смена места в технике
        addNetworkMessage(
            SeatSwapMessage.class,
            SeatSwapMessage::encode,
            SeatSwapMessage::decode,
            SeatSwapMessage::handler,
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}
