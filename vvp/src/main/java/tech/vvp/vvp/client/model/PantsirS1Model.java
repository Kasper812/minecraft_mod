package tech.vvp.vvp.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tech.vvp.vvp.client.PantsirClientHandler;
import tech.vvp.vvp.entity.vehicle.PantsirS1Entity;

/**
 * Модель Pantsir-S1 с вращающимся радаром
 */
public class PantsirS1Model extends VvpVehicleModel<PantsirS1Entity> {

    // Для автономного вращения радара на клиенте (для каждого панциря отдельно)
    private long lastUpdateTime = 0;
    private float localRadarAngle = 0;
    
    // Скорость вращения радара (градусов в миллисекунду)
    // 360° за 42.5 тика = 360° за 2.125 сек = ~169.4°/сек = ~0.1694°/мс
    private static final float RADAR_ROTATION_SPEED = 360.0f / 2125.0f;

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public TransformContext<PantsirS1Entity> collectTransform(String boneName) {
        // Обработка кости радара - плавное вращение
        if (boneName.equals("RADAR")) {
            return (bone, vehicle, state) -> {
                float angle;
                
                // Проверяем сидит ли игрок в ЭТОМ панцире
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;
                if (player != null) {
                    Entity playerVehicle = player.getVehicle();
                    if (playerVehicle == vehicle) {
                        // Игрок в этом панцире - используем синхронизированный угол
                        angle = PantsirClientHandler.getInterpolatedRadarAngle(vehicle.getId());
                    } else {
                        // Игрок НЕ в этом панцире - вращаем локально
                        angle = getLocalRadarAngle();
                    }
                } else {
                    angle = getLocalRadarAngle();
                }
                
                bone.setRotY(angle * Mth.DEG_TO_RAD);
            };
        }
        
        return super.collectTransform(boneName);
    }
    
    /**
     * Возвращает локальный угол радара для панцирей в которых игрок не сидит
     */
    private float getLocalRadarAngle() {
        long currentTime = System.currentTimeMillis();
        
        if (lastUpdateTime == 0) {
            lastUpdateTime = currentTime;
            return localRadarAngle;
        }
        
        float deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        
        // Ограничиваем deltaTime
        deltaTime = Math.min(deltaTime, 50);
        
        // Вращаем радар
        localRadarAngle -= RADAR_ROTATION_SPEED * deltaTime;
        
        // Нормализуем
        while (localRadarAngle < -360) localRadarAngle += 360;
        while (localRadarAngle > 0) localRadarAngle -= 360;
        
        return localRadarAngle;
    }
}
