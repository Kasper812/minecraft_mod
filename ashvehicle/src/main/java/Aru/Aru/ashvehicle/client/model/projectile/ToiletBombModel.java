package Aru.Aru.ashvehicle.client.model.projectile;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.projectile.ToiletBombEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class ToiletBombModel extends GeoModel<ToiletBombEntity> {
    public ToiletBombModel() {
    }

    public Identifier getAnimationResource(ToiletBombEntity entity) {
        return null;
    }

    public Identifier getModelResource(ToiletBombEntity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "geo/toiletbomb.geo.json");
    }

    public Identifier getTextureResource(ToiletBombEntity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "textures/entity/toiletbomb.png");
    }
}