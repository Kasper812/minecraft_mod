package tech.vvp.vvp.client.model;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.entity.vehicle.FMTVEntity;

public class FMTVModel extends VvpVehicleModel<FMTVEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<FMTVEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "steering_wheel" -> (bone, vehicle, state) -> {
                float steeringAngle = Mth.lerp(state.getPartialTick(), vehicle.getPrevSteeringAngle(), vehicle.getSteeringAngle());
                bone.setRotZ((float) Math.toRadians(steeringAngle * 10));
            };
            // Передние колёса с поворотом (wheelL0Turn, wheelR0Turn)
            case "wheelL0Turn", "wheelR0Turn" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));
                
                float steeringAngle = Mth.lerp(state.getPartialTick(), vehicle.getPrevSteeringAngle(), vehicle.getSteeringAngle());
                steeringAngle = Mth.clamp(steeringAngle, -30f, 30f);
                bone.setRotY((float) Math.toRadians(steeringAngle));
            };
            // Задние колёса - только вращение
            case "wheelL0", "wheelR0", "wheelL1", "wheelR1" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));
            };
            default -> super.collectTransform(boneName);
        };
    }
}
