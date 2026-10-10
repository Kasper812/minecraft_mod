package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.entity.vehicle.*;
import Aru.Aru.ashvehicle.client.model.VehicleModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class SapsanModel extends VehicleModel<SapsanEntity> {
    public SapsanModel() {
    }

    @Nullable
    @Override
    public VehicleModel.TransformContext<SapsanEntity> collectTransform(String boneName) {
        return null;
    }

    @Override
    public Identifier getModelResource(SapsanEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "geo/sapsan-grim2.geo.json");
    }

    @Override
    public Identifier getTextureResource(SapsanEntity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "textures/entity/sapsan-grim2.png");
    }

    @Override
    public Identifier getAnimationResource(SapsanEntity animatable) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "animations/sapsan-grim2.animation.json");
    }
}
