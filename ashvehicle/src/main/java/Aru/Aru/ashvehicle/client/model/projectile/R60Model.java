package Aru.Aru.ashvehicle.client.model.projectile;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.projectile.*;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class R60Model extends GeoModel<R60Entity> {
    public R60Model() {
    }

    public Identifier getAnimationResource(R60Entity entity) {
        return null;
    }

    public Identifier getModelResource(R60Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "geo/r60.geo.json");
    }

    public Identifier getTextureResource(R60Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "textures/entity/r60.png");
    }
}