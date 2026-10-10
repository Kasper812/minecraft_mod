package tech.vvp.vvp.client.model;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import oshi.util.tuples.Pair;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import tech.vvp.vvp.client.model.util.ModelBoneTransforms;

import java.util.List;

/**
 * GeckoLib reuses one model instance for every entity of the same type.
 * Reset transform bones when switching to another vehicle so recoil / missile bones do not bleed.
 */
public abstract class VvpVehicleModel<T extends VehicleEntity & GeoAnimatable> extends VehicleModel<T> {

    private int lastRenderedEntityId = Integer.MIN_VALUE;

    @Override
    public void setCustomAnimations(T vehicle, long instanceId, AnimationState<T> animationState) {
        int entityId = vehicle.getId();
        if (entityId != lastRenderedEntityId) {
            lastRenderedEntityId = entityId;
            resetSharedTransformBones();
        }
        super.setCustomAnimations(vehicle, instanceId, animationState);
    }

    private void resetSharedTransformBones() {
        List<Pair<String, TransformContext<T>>> transforms = getTRANSFORMS();
        if (transforms.isEmpty()) {
            return;
        }
        for (Pair<String, TransformContext<T>> pair : transforms) {
            GeoBone bone = getAnimationProcessor().getBone(pair.getA());
            if (bone != null) {
                ModelBoneTransforms.resetForVehicleRender(bone);
            }
        }
    }
}
