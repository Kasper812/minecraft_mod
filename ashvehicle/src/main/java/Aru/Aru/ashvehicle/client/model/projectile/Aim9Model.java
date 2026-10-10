package Aru.Aru.ashvehicle.client.model.projectile;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.projectile.*;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class Aim9Model extends GeoModel<Aim9Entity> {
    public Aim9Model() {
    }

    public Identifier getAnimationResource(Aim9Entity entity) {
        return null;
    }

    public Identifier getModelResource(Aim9Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "geo/aim9.geo.json");
    }

    public Identifier getTextureResource(Aim9Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "textures/entity/aim9.png");
    }
}