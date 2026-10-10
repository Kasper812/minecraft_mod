package tech.vvp.vvp.client.model;

import net.minecraft.resources.Identifier;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.entity.vehicle.Ags30Entity;

public class Ags30Model extends VvpVehicleModel<Ags30Entity> {

    @Override
    public Identifier getModelResource(Ags30Entity animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/ags_30.geo.json");
    }

    @Override
    public Identifier getTextureResource(Ags30Entity animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/entity/ags_30.png");
    }

    @Override
    public Identifier getAnimationResource(Ags30Entity animatable) {
        return null;
    }
}
