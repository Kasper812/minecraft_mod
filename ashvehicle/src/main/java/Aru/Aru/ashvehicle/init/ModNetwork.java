package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.Packet.*;
import net.minecraft.resources.Identifier;
import com.atsuishio.portlib.forge.network.NetworkRegistry;
import com.atsuishio.portlib.forge.network.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Identifier.fromNamespaceAndPath(AshVehicle.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++, SetMissileTargetPacket.class, SetMissileTargetPacket::encode, SetMissileTargetPacket::decode, SetMissileTargetPacket::handle);
        INSTANCE.registerMessage(packetId++, TogglePodPacket.class, TogglePodPacket::encode, TogglePodPacket::decode, TogglePodPacket::handle);
        INSTANCE.registerMessage(packetId++, NukeSkyPacket.class, NukeSkyPacket::encode, NukeSkyPacket::decode, NukeSkyPacket::handle);
        INSTANCE.registerMessage(packetId++, ToggleAc130OrbitPacket.class, ToggleAc130OrbitPacket::encode, ToggleAc130OrbitPacket::decode, ToggleAc130OrbitPacket::handle
        );
    }
}
