package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.client.model.VehicleModel;
import Aru.Aru.ashvehicle.entity.vehicle.B52Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class B52Model extends VehicleModel<B52Entity> {
    public B52Model() {}

    @Nullable
    public VehicleModel.TransformContext<B52Entity> collectTransform(String boneName) {
        TransformContext var1000;
        switch (boneName){
            case "FRGear","BRGear","FRWGear":
                var1000 = (bone, vehicle, state) -> bone.setRotZ(vehicle.gearRot(state.getPartialTick()) * (-(float)Math.PI / 180F));
                break;
            case "FLGear","BLGear","FLWGear":
                var1000 = (bone, vehicle, state) -> bone.setRotZ(vehicle.gearRot(state.getPartialTick()) * ((float)Math.PI / 180F));
                break;
            case "LFlap2":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * ((float)Math.PI / 180F));
                break;
            case "RFlap2":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * ((float)Math.PI / 180F));
                break;
            case "BLFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * ((float)Math.PI / 180F));
                break;
            case "BRFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * ((float)Math.PI / 180F));
                break;
            case "VBFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.getFlap3RotO(), vehicle.getFlap3Rot()), -20.0F, 20.0F) * ((float)Math.PI / 180F));
                break;
            default :
                var1000 = null;
                break;
        }
        return var1000;
    }

    @Override
    public Identifier getModelResource(B52Entity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "geo/b52.geo.json");
    }

    @Override
    public Identifier getTextureResource(B52Entity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "textures/entity/b52.png");
    }

    @Override
    public Identifier getAnimationResource(B52Entity animatable) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "animations/b52.animation.json");
    }
}