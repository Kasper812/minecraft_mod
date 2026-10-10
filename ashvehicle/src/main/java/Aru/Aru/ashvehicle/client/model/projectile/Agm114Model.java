package Aru.Aru.ashvehicle.client.model.projectile;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.projectile.*;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class Agm114Model extends GeoModel<Agm114Entity> {
    public Agm114Model() {
    }

    public Identifier getAnimationResource(Agm114Entity entity) {
        return null;
    }

    public Identifier getModelResource(Agm114Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "geo/agm114.geo.json");
    }

    public Identifier getTextureResource(Agm114Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "textures/entity/agm114.png");
    }
}