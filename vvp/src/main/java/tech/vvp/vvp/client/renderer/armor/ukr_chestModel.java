package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.ukr_chest;

public class ukr_chestModel extends GeoModel<ukr_chest> {
    @Override
    public Identifier getModelResource(ukr_chest object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/armor/ukr_chest.geo.json");
    }

    @Override
    public Identifier getTextureResource(ukr_chest object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/ukr.png");
    }

    @Override
    public Identifier getAnimationResource(ukr_chest animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usachest.animation.json");
    }
} 