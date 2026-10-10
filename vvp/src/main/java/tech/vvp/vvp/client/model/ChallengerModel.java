package tech.vvp.vvp.client.model;

import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.client.model.util.CannonRecoilTransforms;
import tech.vvp.vvp.entity.vehicle.ChallengerEntity;

public class ChallengerModel extends VvpVehicleModel<ChallengerEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<ChallengerEntity> collectTransform(String boneName) {
        TransformContext<ChallengerEntity> recoil = CannonRecoilTransforms.matchBarrel(boneName);
        if (recoil != null) {
            return recoil;
        }
        return super.collectTransform(boneName);
    }
}
