package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.rus_helmet_2;

public class rus_helmet_2Model extends GeoModel<rus_helmet_2> {
    @Override
    public Identifier getModelResource(rus_helmet_2 object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/armor/rus_helmet_2.geo.json");
    }

    @Override
    public Identifier getTextureResource(rus_helmet_2 object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/rus_armor.png");
    }

    @Override
    public Identifier getAnimationResource(rus_helmet_2 animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usachest.animation.json");
    }
}
