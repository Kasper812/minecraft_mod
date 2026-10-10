package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.ukr_v2_helmet;

public class ukr_v2_helmetModel extends GeoModel<ukr_v2_helmet> {
    @Override
    public Identifier getModelResource(ukr_v2_helmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/armor/ukr_v2_helmet.geo.json");
    }

    @Override
    public Identifier getTextureResource(ukr_v2_helmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/ukr_v2.png");
    }

    @Override
    public Identifier getAnimationResource(ukr_v2_helmet animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usachest.animation.json");
    }
} 