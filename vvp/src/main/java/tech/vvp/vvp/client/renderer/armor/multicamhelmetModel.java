package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.multicamhelmet;

public class multicamhelmetModel extends GeoModel<multicamhelmet> {
    @Override
    public Identifier getModelResource(multicamhelmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/multicamhelmet.geo.json");
    }

    @Override
    public Identifier getTextureResource(multicamhelmet object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/multicamhelmet.png");
    }

    @Override
    public Identifier getAnimationResource(multicamhelmet animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usahelmet.animation.json");
    }
} 