package tech.vvp.vvp.client.renderer.item;

import tech.vvp.vvp.client.model.item.Shell122mmCasingModel;
import tech.vvp.vvp.item.Shell122mmCasingItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class Shell122mmCasingRenderer extends GeoItemRenderer<Shell122mmCasingItem> {
    public Shell122mmCasingRenderer() {
        super(new Shell122mmCasingModel());
    }
}
