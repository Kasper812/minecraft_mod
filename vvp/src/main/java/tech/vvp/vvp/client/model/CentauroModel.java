package tech.vvp.vvp.client.model;

import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.client.model.util.CannonRecoilTransforms;
import tech.vvp.vvp.entity.vehicle.CentauroEntity;

public class CentauroModel extends VvpVehicleModel<CentauroEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<CentauroEntity> collectTransform(String boneName) {
        TransformContext<CentauroEntity> recoil = CannonRecoilTransforms.matchBarrel(boneName);
        if (recoil != null) {
            return recoil;
        }
        return super.collectTransform(boneName);
    }
}
