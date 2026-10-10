package tech.vvp.vvp.client.model;

import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.client.model.util.CannonRecoilTransforms;
import tech.vvp.vvp.entity.vehicle.M1A2SepEntity;

public class M1A2SepModel extends VvpVehicleModel<M1A2SepEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<M1A2SepEntity> collectTransform(String boneName) {
        TransformContext<M1A2SepEntity> recoil = CannonRecoilTransforms.matchBarrel(boneName);
        if (recoil != null) {
            return recoil;
        }
        return super.collectTransform(boneName);
    }
}
