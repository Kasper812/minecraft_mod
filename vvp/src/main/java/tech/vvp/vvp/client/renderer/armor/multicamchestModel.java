package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.multicamchest;

public class multicamchestModel extends GeoModel<multicamchest> {
    @Override
    public Identifier getModelResource(multicamchest object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/multicamchest.geo.json");
    }

    @Override
    public Identifier getTextureResource(multicamchest object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/multicamhelmet.png");
    }

    @Override
    public Identifier getAnimationResource(multicamchest animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usachest.animation.json");
    }
} 