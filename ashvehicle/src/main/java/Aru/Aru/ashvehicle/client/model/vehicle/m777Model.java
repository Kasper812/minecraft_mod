package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.entity.vehicle.*;
import Aru.Aru.ashvehicle.client.model.VehicleModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class m777Model extends VehicleModel<m777Entity> {
    public m777Model() {
    }

    @Nullable
    @Override
    public VehicleModel.TransformContext<m777Entity> collectTransform(String boneName) {
        return null;
    }

    @Override
    public Identifier getModelResource(m777Entity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "geo/m_777.geo.json");
    }

    @Override
    public Identifier getTextureResource(m777Entity object) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "textures/entity/m_777.png");
    }

    @Override
    public Identifier getAnimationResource(m777Entity animatable) {
        return Identifier.fromNamespaceAndPath("ashvehicle", "animations/m_777.animation.json");
    }
}