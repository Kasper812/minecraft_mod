package tech.vvp.vvp.client.model;

import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.client.model.util.CannonRecoilTransforms;
import tech.vvp.vvp.entity.vehicle.StrykerEntity;

public class StrykerModel extends VvpVehicleModel<StrykerEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<StrykerEntity> collectTransform(String boneName) {
        TransformContext<StrykerEntity> recoil = CannonRecoilTransforms.match(boneName, "Otkat");
        if (recoil != null) {
            return recoil;
        }
        return super.collectTransform(boneName);
    }
}
