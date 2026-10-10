package Aru.Aru.ashvehicle.client.model.projectile;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.projectile.Aim9Entity;
import Aru.Aru.ashvehicle.entity.projectile.BallisticMissileEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class BallisticMissileModel extends GeoModel<BallisticMissileEntity> {
    public BallisticMissileModel() {
    }

    public Identifier getAnimationResource(BallisticMissileEntity entity) {
        return null;
    }

    public Identifier getModelResource(BallisticMissileEntity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "geo/aim9.geo.json");
    }

    public Identifier getTextureResource(BallisticMissileEntity entity) {
        return Identifier.fromNamespaceAndPath(AshVehicle.MODID, "textures/entity/aim9.png");
    }
}
