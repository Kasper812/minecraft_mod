package tech.vvp.vvp.client.model;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.entity.vehicle.Mi8Entity;

public class Mi8Model extends VvpVehicleModel<Mi8Entity> {

    @Override
    public Identifier getModelResource(Mi8Entity entity) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/mi8.geo.json");
    }

    @Override
    public Identifier getTextureResource(Mi8Entity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        
        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }
        
        return textures[0];
    }

    @Override
    public Identifier getAnimationResource(Mi8Entity entity) {
        return null; // No animations file needed
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<Mi8Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "vint2" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            default -> super.collectTransform(boneName);
        };
    }
}
