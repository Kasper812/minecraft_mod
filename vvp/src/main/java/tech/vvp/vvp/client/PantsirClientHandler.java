package tech.vvp.vvp.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import tech.vvp.vvp.entity.vehicle.PantsirS1Entity;
import tech.vvp.vvp.network.message.PantsirRadarSyncMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Клиентский обработчик данных радара Pantsir
 * Хранит данные по ID панциря для поддержки мультиплеера
 */
@Environment(EnvType.CLIENT)
public class PantsirClientHandler {
    
    /**
     * Данные радара конкретного Панциря
     */
    public static class PantsirRadarData {
        public int radarState = PantsirRadarSyncMessage.STATE_IDLE;
        public int targetEntityId = -1;
        public double targetX = 0;
        public double targetY = 0;
        public double targetZ = 0;
        public double targetVelX = 0;
        public double targetVelY = 0;
        public double targetVelZ = 0;
        public int lockProgress = 0;
        public double targetDistance = 0;
        public float radarAngle = 0;
        public float turretAngle = 0;
        public final List<RadarTarget> allTargets = new ArrayList<>();
        public final List<MissilePosition> missiles = new ArrayList<>();
        
        // Для плавной интерполяции радара
        public float clientRadarAngle = 0;
        public float serverRadarAngle = 0;
        public long lastSyncTime = 0;
        public long lastUpdateTime = 0;
        
        // Система потери сигнала для GUI (упрощённая)
        public Vec3 uiLastTargetPos = null;  // Последняя известная позиция
        public boolean uiLostSignal = false; // Флаг потери сигнала
        
        // TTL для очистки
        public long lastMessageTime = System.currentTimeMillis();
    }
    
    // Данные по ID панциря (для мультиплеера)
    private static final Map<Integer, PantsirRadarData> radarDataByVehicle = new HashMap<>();
    
    // Скорость вращения радара (градусов в миллисекунду)
    private static final float RADAR_ROTATION_SPEED = 360.0f / 2125.0f;
    
    /**
     * Данные о цели на радаре
     */
    public static class RadarTarget {
        public final int entityId;
        public final double x, y, z;
        public final int targetType;
        public final boolean isAlly;
        
        public RadarTarget(int entityId, double x, double y, double z, int targetType, boolean isAlly) {
            this.entityId = entityId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.targetType = targetType;
            this.isAlly = isAlly;
        }
    }
    
    /**
     * Данные о ракете на радаре
     */
    public static class MissilePosition {
        public final double x, y, z;
        
        public MissilePosition(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    /**
     * Обработка сообщения синхронизации радара от сервера
     * Сохраняет данные по ID панциря для поддержки мультиплеера
     */
    public static void handleRadarSync(PantsirRadarSyncMessage message) {
        // Получаем или создаём данные для этого панциря
        PantsirRadarData data = radarDataByVehicle.computeIfAbsent(message.vehicleId, k -> new PantsirRadarData());
        
        // Обновляем данные
        data.radarState = message.radarState;
        data.targetEntityId = message.targetEntityId;
        data.targetX = message.targetX;
        data.targetY = message.targetY;
        data.targetZ = message.targetZ;
        data.targetVelX = message.targetVelX;
        data.targetVelY = message.targetVelY;
        data.targetVelZ = message.targetVelZ;
        data.lockProgress = message.lockProgress;
        data.targetDistance = message.targetDistance;
        
        // Синхронизируем угол с сервером
        data.serverRadarAngle = message.radarAngle;
        data.clientRadarAngle = message.radarAngle;
        data.lastSyncTime = System.currentTimeMillis();
        
        data.turretAngle = message.turretAngle;
        
        // Обновляем список целей
        data.allTargets.clear();
        for (int i = 0; i < message.allTargetIds.length; i++) {
            data.allTargets.add(new RadarTarget(
                message.allTargetIds[i],
                message.allTargetX[i],
                message.allTargetY[i],
                message.allTargetZ[i],
                message.allTargetTypes[i],
                message.allTargetIsAlly[i]
            ));
        }
        
        // Обновляем список ракет
        data.missiles.clear();
        for (int i = 0; i < message.missileX.length; i++) {
            data.missiles.add(new MissilePosition(
                message.missileX[i],
                message.missileY[i],
                message.missileZ[i]
            ));
        }
        
        // Обработка потери сигнала для GUI
        if (message.signalLost) {
            // Сигнал потерян - сохраняем последнюю позицию
            data.uiLostSignal = true;
            data.uiLastTargetPos = new Vec3(message.lostTargetX, message.lostTargetY, message.lostTargetZ);
        } else {
            // Сигнал восстановлен или цель в зоне - сбрасываем
            data.uiLostSignal = false;
            data.uiLastTargetPos = null;
        }
        
        // Обновляем TTL
        data.lastMessageTime = System.currentTimeMillis();
        
        // Очистка старых данных (TTL > 5 секунд)
        cleanupOldData();
    }
    
    /**
     * Очищает данные панцирей которые не обновлялись > 5 секунд
     */
    private static void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        radarDataByVehicle.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().lastMessageTime > 5000
        );
    }
    
    /**
     * Получает данные радара для конкретного панциря
     */
    public static PantsirRadarData getRadarData(int vehicleId) {
        return radarDataByVehicle.get(vehicleId);
    }
    
    /**
     * Возвращает интерполированный угол радара для конкретного панциря
     */
    public static float getInterpolatedRadarAngle(int vehicleId) {
        PantsirRadarData data = radarDataByVehicle.get(vehicleId);
        if (data == null) return 0;
        
        long currentTime = System.currentTimeMillis();
        
        if (data.lastUpdateTime == 0) {
            data.lastUpdateTime = currentTime;
            return data.clientRadarAngle;
        }
        
        float deltaTime = currentTime - data.lastUpdateTime;
        data.lastUpdateTime = currentTime;
        deltaTime = Math.min(deltaTime, 50);
        
        data.clientRadarAngle -= RADAR_ROTATION_SPEED * deltaTime;
        
        while (data.clientRadarAngle < -360) {
            data.clientRadarAngle += 360;
        }
        while (data.clientRadarAngle > 0) {
            data.clientRadarAngle -= 360;
        }
        
        return data.clientRadarAngle;
    }
    
    /**
     * Сброс всех данных
     */
    public static void reset() {
        radarDataByVehicle.clear();
    }
    
    /**
     * Проверяет, захвачена ли цель (для конкретного панциря)
     */
    public static boolean isTargetLocked(int vehicleId) {
        PantsirRadarData data = radarDataByVehicle.get(vehicleId);
        return data != null && data.radarState == PantsirRadarSyncMessage.STATE_LOCKED;
    }
    
    /**
     * Проверяет, идёт ли процесс захвата (для конкретного панциря)
     */
    public static boolean isLocking(int vehicleId) {
        PantsirRadarData data = radarDataByVehicle.get(vehicleId);
        return data != null && data.radarState == PantsirRadarSyncMessage.STATE_LOCKING;
    }
    
    /**
     * Проверяет, обнаружена ли основная цель (для конкретного панциря)
     */
    public static boolean isTargetDetected(int vehicleId) {
        PantsirRadarData data = radarDataByVehicle.get(vehicleId);
        if (data == null) return false;
        return data.targetEntityId != -1 && (
            data.radarState == PantsirRadarSyncMessage.STATE_DETECTED ||
            data.radarState == PantsirRadarSyncMessage.STATE_LOCKING ||
            data.radarState == PantsirRadarSyncMessage.STATE_LOCKED
        );
    }
    
    /**
     * Проверяет, есть ли цели на радаре (для конкретного панциря)
     */
    public static boolean hasTargets(int vehicleId) {
        PantsirRadarData data = radarDataByVehicle.get(vehicleId);
        return data != null && !data.allTargets.isEmpty();
    }
}
