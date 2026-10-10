package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.entity.vehicle.*;
import Aru.Aru.ashvehicle.client.model.VehicleModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class Tom7Model extends VehicleModel<Tom7Entity> {
    public Tom7Model() {
    }

    @Nullable
    @Override
    public VehicleModel.TransformContext<Tom7Entity> collectTransform(String boneName) {
        return null;
    }

    @Override
    public Identifier getModelResource(Tom7Entity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "geo/tom_7.geo.json");
    }

    @Override
    public Identifier getTextureResource(Tom7Entity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "textures/entity/tom_7.png");
    }

    @Override
    public Identifier getAnimationResource(Tom7Entity animatable) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "animations/tom_7.animation.json");
    }
}
