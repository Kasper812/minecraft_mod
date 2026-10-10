package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.bereta;

public class beretaModel extends GeoModel<bereta> {
    @Override
    public Identifier getModelResource(bereta object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/bereta.geo.json");
    }

    @Override
    public Identifier getTextureResource(bereta object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/kepki.png");
    }

    @Override
    public Identifier getAnimationResource(bereta animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usahelmet.animation.json");
    }
} 