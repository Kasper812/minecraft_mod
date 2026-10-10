package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.crewhelmet;

public class crewModel extends GeoModel<crewhelmet> {
    @Override
    public Identifier getModelResource(crewhelmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/crew.geo.json");
    }

    @Override
    public Identifier getTextureResource(crewhelmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/crew.png");
    }

    @Override
    public Identifier getAnimationResource(crewhelmet animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usahelmet.animation.json");
    }
} 