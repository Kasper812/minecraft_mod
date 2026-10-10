package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.item.armor.pmc_chest;

public class pmc_chestModel extends GeoModel<pmc_chest> {
    @Override
    public Identifier getModelResource(pmc_chest object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "geo/armor/pmc_armor.geo.json");
    }

    @Override
    public Identifier getTextureResource(pmc_chest object) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "textures/armor/pmc.png");
    }

    @Override
    public Identifier getAnimationResource(pmc_chest animatable) {
        return Identifier.fromNamespaceAndPath(VVP.MOD_ID, "animations/usachest.animation.json");
    }
} 