package frontline.combat.fcp.client.model.Util;

import software.bernie.geckolib.cache.object.GeoBone;

public final class ModelBoneTransforms {

    private ModelBoneTransforms() {
    }

    /** Clears per-frame overrides on shared GeckoLib bones before applying another vehicle's state. */
    public static void resetForVehicleRender(GeoBone bone) {
        bone.setHidden(false);
        bone.setPosX(0f);
        bone.setPosY(0f);
        bone.setPosZ(0f);
        bone.setRotX(0f);
        bone.setRotY(0f);
        bone.setRotZ(0f);
        bone.setScaleX(1f);
        bone.setScaleY(1f);
        bone.setScaleZ(1f);
    }

    public static void clearRecoilOffsets(GeoBone bone) {
        bone.setPosZ(0f);
        bone.setRotX(0f);
    }
}