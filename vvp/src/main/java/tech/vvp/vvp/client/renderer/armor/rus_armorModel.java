package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.rus_armor;

public class rus_armorModel extends GeoModel<rus_armor> {
    @Override
    public Identifier getModelResource(rus_armor object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/armor/rus_armor.geo.json");
    }

    @Override
    public Identifier getTextureResource(rus_armor object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/rus_armor.png");
    }

    @Override
    public Identifier getAnimationResource(rus_armor animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usachest.animation.json");
    }
}
