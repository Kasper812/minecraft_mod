package tech.vvp.vvp.client.model;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.entity.vehicle.D30Entity;

public class D30Model extends VvpVehicleModel<D30Entity> {

    private int lastRenderedEntityId = Integer.MIN_VALUE;
    private float vertelYawRotation = 0f;
    private float vertelPitchRotation = 0f;
    private float prevTurretYaw = 0f;
    private float prevTurretPitch = 0f;

    @Override
    public Identifier getModelResource(D30Entity entity) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/d30.geo.json");
    }

    @Override
    public Identifier getTextureResource(D30Entity entity) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/entity/d30.png");
    }

    @Override
    public Identifier getAnimationResource(D30Entity entity) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/d30.animation.json");
    }

    @Override
    public void setCustomAnimations(D30Entity vehicle, long instanceId, AnimationState<D30Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);

        int entityId = vehicle.getId();
        if (entityId != lastRenderedEntityId) {
            lastRenderedEntityId = entityId;
            vertelYawRotation = 0f;
            vertelPitchRotation = 0f;
            prevTurretYaw = vehicle.getTurretYRot();
            prevTurretPitch = vehicle.getTurretXRot();
        }

        float currentYaw = vehicle.getTurretYRot();
        float currentPitch = vehicle.getTurretXRot();
        
        // Дельта для yaw
        float deltaYaw = currentYaw - prevTurretYaw;
        while (deltaYaw > 180) deltaYaw -= 360;
        while (deltaYaw < -180) deltaYaw += 360;
        vertelYawRotation += deltaYaw * 0.5f;
        prevTurretYaw = currentYaw;
        
        // Дельта для pitch
        float deltaPitch = currentPitch - prevTurretPitch;
        vertelPitchRotation += deltaPitch * 0.5f;
        prevTurretPitch = currentPitch;
        
        // Применяем к костям
        var boneYaw = getAnimationProcessor().getBone("vertelkanekrutoi");
        if (boneYaw != null) {
            boneYaw.setRotZ(vertelYawRotation * Mth.DEG_TO_RAD);
        }
        
        var bonePitch = getAnimationProcessor().getBone("vertelkakrytai");
        if (bonePitch != null) {
            bonePitch.setRotX(vertelPitchRotation * Mth.DEG_TO_RAD);
        }
    }
}
