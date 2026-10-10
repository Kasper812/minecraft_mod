package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.kepka;

public class kepkaModel extends GeoModel<kepka> {
    @Override
    public Identifier getModelResource(kepka object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/kepka.geo.json");
    }

    @Override
    public Identifier getTextureResource(kepka object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/kepki.png");
    }

    @Override
    public Identifier getAnimationResource(kepka animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usahelmet.animation.json");
    }
} 