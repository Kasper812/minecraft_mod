package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.client.model.VehicleModel;
import Aru.Aru.ashvehicle.entity.vehicle.ZelenskyEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ZelenskyModel extends VehicleModel<ZelenskyEntity> {
    public ZelenskyModel() {
    }

    @Nullable
    public VehicleModel.TransformContext<ZelenskyEntity> collectTransform(String boneName) {
        TransformContext var1000;
        switch (boneName){
            case "Pro":
                var1000 = (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
                break;
            case "LFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * ((float)Math.PI / 180F));
                break;
            case "RFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * ((float)Math.PI / 180F));
                break;
            case "BLFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * ((float)Math.PI / 180F));
                break;
            case "BRFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * ((float)Math.PI / 180F));
                break;
            case "bomb":
                var1000 = (bone, vehicle, state) -> {
                    var gunData = vehicle.getGunData("MegaBomb");
                    bone.setHidden(gunData == null || gunData.ammo.get() < 1);
                };
                break;
            default :
                var1000 = null;
                break;
        }
        return var1000;
    }
    @Override
    public Identifier getModelResource(ZelenskyEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "geo/zelensky.geo.json");
    }

    @Override
    public Identifier getTextureResource(ZelenskyEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "textures/entity/zelensky.png");
    }

    @Override
    public Identifier getAnimationResource(ZelenskyEntity animatable) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "animations/ah-64.animation.json");
    }
}