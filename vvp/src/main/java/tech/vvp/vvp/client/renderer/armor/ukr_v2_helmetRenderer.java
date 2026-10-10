package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import tech.vvp.vvp.item.armor.ukr_v2_helmet;

public class ukr_v2_helmetRenderer extends GeoArmorRenderer<ukr_v2_helmet> {
    public ukr_v2_helmetRenderer() {
        super(new ukr_v2_helmetModel());
    }

    @Override
    public RenderType getRenderType(ukr_v2_helmet animatable, Identifier texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityTranslucent(getTextureLocation(animatable));
    }
}
