package tech.vvp.vvp.client.model;

import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.client.model.util.CannonRecoilTransforms;
import tech.vvp.vvp.entity.vehicle.Bmp3Entity;

public class Bmp3Model extends VvpVehicleModel<Bmp3Entity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<Bmp3Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "dulo" -> CannonRecoilTransforms.weaponRecoil(boneName, "dulo", "Cannon");
            case "dasdas" -> CannonRecoilTransforms.weaponRecoil(boneName, "dasdas", "Missile");
            default -> super.collectTransform(boneName);
        };
    }
}
