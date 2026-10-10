package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.rus_helmet;

public class rus_helmetModel extends GeoModel<rus_helmet> {
    @Override
    public Identifier getModelResource(rus_helmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/armor/rus_helmet.geo.json");
    }

    @Override
    public Identifier getTextureResource(rus_helmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/rus_armor.png");
    }

    @Override
    public Identifier getAnimationResource(rus_helmet animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usachest.animation.json");
    }
}
