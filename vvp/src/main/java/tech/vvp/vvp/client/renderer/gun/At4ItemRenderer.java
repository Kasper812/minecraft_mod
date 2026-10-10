package tech.vvp.vvp.client.renderer.gun;

import tech.vvp.vvp.client.model.item.At4ItemModel;
import tech.vvp.vvp.item.gun.At4Item;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class At4ItemRenderer extends GeoItemRenderer<At4Item> {
    public At4ItemRenderer() {
        super(new At4ItemModel());
    }
}
