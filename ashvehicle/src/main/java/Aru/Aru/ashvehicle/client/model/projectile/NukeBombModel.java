package Aru.Aru.ashvehicle.client.model.projectile;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.projectile.NukeBombEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class NukeBombModel extends GeoModel<NukeBombEntity> {
    public NukeBombModel() {
    }

    public Identifier getAnimationResource(NukeBombEntity entity) {
        return null;
    }

    public Identifier getModelResource(NukeBombEntity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "geo/nuclearbomb.geo.json");
    }

    public Identifier getTextureResource(NukeBombEntity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "textures/entity/nuclearbomb.png");
    }
}
