package tech.vvp.vvp.network.message;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import com.atsuishio.portlib.forge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Сообщение для смены места в технике
 * Клиент -> Сервер
 */
public class SeatSwapMessage {
    
    private final int targetSeatIndex;
    
    public SeatSwapMessage(int targetSeatIndex) {
        this.targetSeatIndex = targetSeatIndex;
    }
    
    public static void encode(SeatSwapMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.targetSeatIndex);
    }
    
    public static SeatSwapMessage decode(FriendlyByteBuf buffer) {
        return new SeatSwapMessage(buffer.readInt());
    }
    
    public static void handler(SeatSwapMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            
            Entity vehicle = player.getVehicle();
            if (!(vehicle instanceof VehicleEntity vehicleEntity)) return;
            
            int targetSeat = message.targetSeatIndex;
            
            // Проверяем что место существует и свободно
            if (targetSeat < 0 || targetSeat >= vehicleEntity.getMaxPassengers()) return;
            
            // Получаем текущее место игрока
            int currentSeat = vehicleEntity.getSeatIndex(player);
            if (currentSeat == targetSeat) return; // Уже на этом месте
            
            // Проверяем что целевое место свободно
            List<Entity> passengers = vehicleEntity.getOrderedPassengers();
            Entity occupant = (targetSeat < passengers.size()) ? passengers.get(targetSeat) : null;
            if (occupant != null && occupant != player) return; // Место занято
            
            // Меняем место через метод SuperbWarfare
            vehicleEntity.changeSeat(player, targetSeat);
        });
        ctx.get().setPacketHandled(true);
    }
}
