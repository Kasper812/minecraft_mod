package Aru.Aru.ashvehicle.client.model.projectile;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.projectile.Aim54Entity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class Aim54Model extends GeoModel<Aim54Entity> {
    public Aim54Model() {
    }

    public Identifier getAnimationResource(Aim54Entity entity) {
        return null;
    }

    public Identifier getModelResource(Aim54Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "geo/aim54.geo.json");
    }

    public Identifier getTextureResource(Aim54Entity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "textures/entity/aim54.png");
    }
}