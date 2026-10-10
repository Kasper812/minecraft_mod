package tech.vvp.vvp.client.model.item;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.vvp.vvp.item.Shell122mmCasingItem;

public class Shell122mmCasingModel extends GeoModel<Shell122mmCasingItem> {
    @Override
    public Identifier getModelResource(Shell122mmCasingItem animatable) {
        return Identifier.fromNamespaceAndPath("vvp", "geo/shell_122mm_casing.geo.json");
    }

    @Override
    public Identifier getTextureResource(Shell122mmCasingItem animatable) {
        return Identifier.fromNamespaceAndPath("vvp", "textures/item/shell_122mm_casing.png");
    }

    @Override
    public Identifier getAnimationResource(Shell122mmCasingItem animatable) {
        return null;
    }
}
