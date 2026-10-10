package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.entity.vehicle.*;
import Aru.Aru.ashvehicle.client.model.VehicleModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class RubberBoatModel extends VehicleModel<RubberBoatEntity> {
    public RubberBoatModel() {
    }

    @Nullable
    @Override
    public VehicleModel.TransformContext<RubberBoatEntity> collectTransform(String boneName) {
        return null;
    }

    @Override
    public Identifier getModelResource(RubberBoatEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "geo/rubber_boat.geo.json");
    }

    @Override
    public Identifier getTextureResource(RubberBoatEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "textures/entity/rubber_boat.png");
    }

    @Override
    public Identifier getAnimationResource(RubberBoatEntity animatable) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "animations/rubber_boat.animation.json");
    }
}