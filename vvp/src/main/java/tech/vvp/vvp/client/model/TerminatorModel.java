package tech.vvp.vvp.client.model;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.client.model.util.CannonRecoilTransforms;
import tech.vvp.vvp.client.model.util.ModelBoneTransforms;
import tech.vvp.vvp.entity.vehicle.TerminatorEntity;

public class TerminatorModel extends VvpVehicleModel<TerminatorEntity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<TerminatorEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "rocket" -> missileBone(4);
            case "rocket3" -> missileBone(3);
            case "rocket2" -> missileBone(2);
            case "rocket4" -> missileBone(1);
            case "dulo" -> barrelRecoil(0);
            case "dulo2" -> barrelRecoil(1);
            default -> super.collectTransform(boneName);
        };
    }

    private TransformContext<TerminatorEntity> missileBone(int minAmmo) {
        return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, minAmmo));
    }

    private TransformContext<TerminatorEntity> barrelRecoil(int barrelIndex) {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) {
                return;
            }
            if (!CANNON_WEAPON.equals(vehicle.getGunName(0))) {
                return;
            }
            var gunData = vehicle.getGunData(CANNON_WEAPON);
            if (gunData == null) {
                return;
            }
            int firedBarrelIndex = Math.floorMod(gunData.fireIndex.get() - 1, 2);
            if (firedBarrelIndex != barrelIndex) {
                return;
            }
            CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.STANDARD);
        };
    }

    private boolean shouldHideMissile(TerminatorEntity vehicle, int minAmmo) {
        var gunData = vehicle.getGunData("Missile");
        return gunData != null && gunData.ammo.get() < minAmmo;
    }

    private static final int TRACK_COUNT = 53;
    private static final int MAX_IDX = 53;
    private static final float[][] KEYFRAMES = {
            {176.78f, 21.97f, 51.55f},
            {128.03f, 19.86f, 55.87f},
            {78.54f, 15.17f, 56.88f},
            {31.65f, 11.50f, 53.78f},
            {31.65f, 8.89f, 49.54f},
            {31.65f, 6.28f, 45.31f},
            {33.51f, 3.67f, 41.08f},
            {6.90f, 1.72f, 36.59f},
            {0.00f, 1.69f, 31.62f},
            {0.00f, 1.69f, 26.65f},
            {0.00f, 1.69f, 21.67f},
            {0.00f, 1.69f, 16.70f},
            {0.00f, 1.69f, 11.73f},
            {0.00f, 1.69f, 6.75f},
            {0.00f, 1.69f, 1.78f},
            {0.00f, 1.69f, -3.19f},
            {0.00f, 1.69f, -8.16f},
            {0.00f, 1.69f, -13.14f},
            {0.00f, 1.69f, -18.11f},
            {0.00f, 1.69f, -23.08f},
            {0.00f, 1.69f, -28.06f},
            {0.00f, 1.69f, -33.03f},
            {0.00f, 1.69f, -38.00f},
            {0.00f, 1.69f, -42.97f},
            {-0.57f, 1.69f, -47.95f},
            {-34.42f, 3.17f, -52.60f},
            {-43.88f, 6.60f, -56.20f},
            {-43.88f, 10.05f, -59.78f},
            {-43.88f, 13.49f, -63.37f},
            {-92.83f, 17.95f, -65.12f},
            {-147.62f, 22.07f, -62.65f},
            {-178.31f, 22.94f, -57.85f},
            {179.48f, 22.91f, -52.87f},
            {179.48f, 22.86f, -47.90f},
            {179.48f, 22.82f, -42.93f},
            {179.48f, 22.77f, -37.96f},
            {179.48f, 22.73f, -32.98f},
            {179.48f, 22.68f, -28.01f},
            {179.48f, 22.64f, -23.04f},
            {179.48f, 22.60f, -18.07f},
            {179.48f, 22.55f, -13.09f},
            {179.48f, 22.51f, -8.12f},
            {179.48f, 22.46f, -3.15f},
            {179.48f, 22.42f, 1.82f},
            {179.48f, 22.37f, 6.80f},
            {179.48f, 22.33f, 11.77f},
            {179.48f, 22.28f, 16.74f},
            {179.48f, 22.24f, 21.72f},
            {179.48f, 22.19f, 26.69f},
            {179.48f, 22.15f, 31.66f},
            {179.48f, 22.10f, 36.63f},
            {179.48f, 22.06f, 41.61f},
            {179.48f, 22.01f, 46.58f},
            {176.78f, 21.97f, 51.55f}
    };

    private static final float START_Y = 21.97f;
    private static final float START_Z = 51.55f;

    private float getKeyframeValue(float t, int component) {
        int wrapRange = TRACK_COUNT * 2;
        float normalized = (t / wrapRange) * MAX_IDX;
        int idx1 = Mth.clamp((int) normalized, 0, MAX_IDX);
        int idx2 = Mth.clamp(idx1 + 1, 0, MAX_IDX);
        float frac = normalized - (int) normalized;

        float p1 = KEYFRAMES[idx1][component];
        float p2 = KEYFRAMES[idx2][component];

        if (component == 0) {
            float diff = p2 - p1;
            if (diff > 180f) p2 -= 360f;
            else if (diff < -180f) p2 += 360f;
        }

        return Mth.lerp(frac, p1, p2);
    }

    @Override
    public float getBoneRotX(float t) {
        return getKeyframeValue(t, 0);
    }

    @Override
    public float getBoneMoveY(float t) {
        return getKeyframeValue(t, 1) - START_Y;
    }

    @Override
    public float getBoneMoveZ(float t) {
        return getKeyframeValue(t, 2) - START_Z;
    }
}
