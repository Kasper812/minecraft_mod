package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import tech.vvp.vvp.item.armor.panama;

public class panamaRenderer extends GeoArmorRenderer<panama> {
    public panamaRenderer() {
        super(new panamaModel());
    }

    @Override
    public RenderType getRenderType(panama animatable, Identifier texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityTranslucent(getTextureLocation(animatable));
    }
}
