package tech.vvp.vvp.client.model;

import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.client.model.util.CannonRecoilTransforms;
import tech.vvp.vvp.entity.vehicle.T90MEntity;

public class T90MModel extends VvpVehicleModel<T90MEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<T90MEntity> collectTransform(String boneName) {
        TransformContext<T90MEntity> recoil = CannonRecoilTransforms.match(boneName, "dulo_cannon");
        if (recoil != null) {
            return recoil;
        }
        return super.collectTransform(boneName);
    }
}
