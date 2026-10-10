package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.entity.vehicle.*;
import Aru.Aru.ashvehicle.client.model.VehicleModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class M1A1AbramsModel extends VehicleModel<M1A1AbramsEntity> {
    public M1A1AbramsModel() {
    }

    @Nullable
    public VehicleModel.TransformContext<M1A1AbramsEntity> collectTransform(String boneName) {
        TransformContext var1000;
        switch (boneName){
            case "turret" :
                var1000 = (bone, vehicle, state) -> bone.setRotY(this.turretYRot * ((float)Math.PI / 180F));
                break;
            case "barrel":
                float a = this.turretYaw;
                float r = (Mth.abs(a) - 90.0F) / 90.0F;
                float r2;
                if (Mth.abs(a) <= 90.0F) {
                    r2 = a / 90.0F;
                } else if (a < 0.0F) {
                    r2 = -(180.0F + a) / 90.0F;
                } else {
                    r2 = (180.0F - a) / 90.0F;
                }
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.clamp(this.turretXRot - r * this.pitch - r2 * this.roll, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * ((float)Math.PI / 180F));
                break;
            default :
                var1000 = null;
                break;
        }
        return var1000;
    }

    @Override
    public Identifier getModelResource(M1A1AbramsEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "geo/m1a1abrams.geo.json");
    }

    @Override
    public Identifier getTextureResource(M1A1AbramsEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "textures/entity/m1a1abrams.png");
    }

    @Override
    public Identifier getAnimationResource(M1A1AbramsEntity animatable) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "animations/m1a1abrams.animation.json");
    }
}