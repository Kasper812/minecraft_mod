package Aru.Aru.ashvehicle.client.model.projectile;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.projectile.Cbu87Entity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class Cbu87Model extends GeoModel<Cbu87Entity> {
    public Cbu87Model() {
    }

    public Identifier getAnimationResource(Cbu87Entity entity) {
        return null;
    }

    public Identifier getModelResource(Cbu87Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "geo/cbu87.geo.json");
    }

    public Identifier getTextureResource(Cbu87Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "textures/entity/cbu87.png");
    }
}