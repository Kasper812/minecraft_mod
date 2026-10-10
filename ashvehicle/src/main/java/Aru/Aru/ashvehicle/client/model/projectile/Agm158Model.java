package Aru.Aru.ashvehicle.client.model.projectile;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.projectile.Agm158Entity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class Agm158Model extends GeoModel<Agm158Entity> {
    public Agm158Model() {
    }

    public Identifier getAnimationResource(Agm158Entity entity) {
        return null;
    }

    public Identifier getModelResource(Agm158Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "geo/agm158.geo.json");
    }

    public Identifier getTextureResource(Agm158Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "textures/entity/agm158.png");
    }
}