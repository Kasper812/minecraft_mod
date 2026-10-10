package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import tech.vvp.vvp.item.armor.pmc_helmet;

public class pmc_helmetRenderer extends GeoArmorRenderer<pmc_helmet> {
    public pmc_helmetRenderer() {
        super(new pmc_helmetModel());
    }

    @Override
    public RenderType getRenderType(pmc_helmet animatable, Identifier texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityTranslucent(getTextureLocation(animatable));
    }
}
