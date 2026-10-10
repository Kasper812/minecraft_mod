package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.rus_helmet_3;

public class rus_helmet_3Model extends GeoModel<rus_helmet_3> {
    @Override
    public Identifier getModelResource(rus_helmet_3 object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/armor/rus_helmet_3.geo.json");
    }

    @Override
    public Identifier getTextureResource(rus_helmet_3 object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/rus_helmet_3.png");
    }

    @Override
    public Identifier getAnimationResource(rus_helmet_3 animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usachest.animation.json");
    }
}
