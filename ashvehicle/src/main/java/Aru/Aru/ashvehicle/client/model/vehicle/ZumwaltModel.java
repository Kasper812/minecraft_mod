package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.entity.vehicle.*;
import Aru.Aru.ashvehicle.client.model.VehicleModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class ZumwaltModel extends VehicleModel<ZumwaltEntity> {
    public ZumwaltModel() {
    }

    @Nullable
    @Override
    public VehicleModel.TransformContext<ZumwaltEntity> collectTransform(String boneName) {
        return null;
    }

    @Override
    public Identifier getModelResource(ZumwaltEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "geo/zumwalt.geo.json");
    }

    @Override
    public Identifier getTextureResource(ZumwaltEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "textures/entity/zumwalt.png");
    }

    @Override
    public Identifier getAnimationResource(ZumwaltEntity animatable) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "animations/zumwalt.animation.json");
    }
}
