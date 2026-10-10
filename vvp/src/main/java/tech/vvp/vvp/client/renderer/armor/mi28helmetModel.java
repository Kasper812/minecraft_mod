package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.mi28helmet;

public class mi28helmetModel extends GeoModel<mi28helmet> {
    @Override
    public Identifier getModelResource(mi28helmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/mi28_helmet.geo.json");
    }

    @Override
    public Identifier getTextureResource(mi28helmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/mi28_armor.png");
    }

    @Override
    public Identifier getAnimationResource(mi28helmet animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usahelmet.animation.json");
    }
} 