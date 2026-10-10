package tech.vvp.vvp.client.model;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.entity.vehicle.Nh90Entity;

public class Nh90Model extends VvpVehicleModel<Nh90Entity> {

    @Override
    public Identifier getModelResource(Nh90Entity entity) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/nh90.geo.json");
    }

    @Override
    public Identifier getTextureResource(Nh90Entity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        
        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }
        
        return textures[0];
    }

    @Override
    public Identifier getAnimationResource(Nh90Entity entity) {
        return null; // No animations file needed
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<Nh90Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "wing" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "tailPropeller" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            default -> super.collectTransform(boneName);
        };
    }
}