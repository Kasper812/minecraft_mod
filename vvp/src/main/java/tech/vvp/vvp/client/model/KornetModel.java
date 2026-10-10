package tech.vvp.vvp.client.model;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.entity.vehicle.KornetEntity;

public class KornetModel extends VvpVehicleModel<KornetEntity> {
    @Override
    public @Nullable TransformContext<KornetEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "guanmiao" -> (bone, vehicle, state) -> {
                var player = Minecraft.getInstance().player;
                bone.setHidden(vehicle.getFirstPassenger() == player && (Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON || ClientEventHandler.zoomVehicle));
            };
            case "missile" -> (bone, vehicle, state) -> bone.setHidden(!vehicle.getEntityData().get(KornetEntity.LOADED));
            default -> super.collectTransform(boneName);
        };
    }
}