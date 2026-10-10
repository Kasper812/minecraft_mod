package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import tech.vvp.vvp.item.armor.ukr_helmet;

public class ukr_helmetRenderer extends GeoArmorRenderer<ukr_helmet> {
    public ukr_helmetRenderer() {
        super(new ukr_helmetModel());
    }

    @Override
    public RenderType getRenderType(ukr_helmet animatable, Identifier texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityTranslucent(getTextureLocation(animatable));
    }
}
