package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.panama;

public class panamaModel extends GeoModel<panama> {
    @Override
    public Identifier getModelResource(panama object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/panama.geo.json");
    }

    @Override
    public Identifier getTextureResource(panama object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/kepki.png");
    }

    @Override
    public Identifier getAnimationResource(panama animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usahelmet.animation.json");
    }
} 