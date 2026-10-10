package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.mi28chest;

public class mi28chestModel extends GeoModel<mi28chest> {
    @Override
    public Identifier getModelResource(mi28chest object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/mi28_armor.geo.json");
    }

    @Override
    public Identifier getTextureResource(mi28chest object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/mi28_armor.png");
    }

    @Override
    public Identifier getAnimationResource(mi28chest animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usachest.animation.json");
    }
} 