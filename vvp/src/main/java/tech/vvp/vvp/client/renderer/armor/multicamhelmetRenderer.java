package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import tech.vvp.vvp.item.armor.multicamhelmet;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class multicamhelmetRenderer extends GeoArmorRenderer<multicamhelmet> {
    public multicamhelmetRenderer() {
        super(new multicamhelmetModel());
    }

    @Override
    public RenderType getRenderType(multicamhelmet animatable, Identifier texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityTranslucent(getTextureLocation(animatable));
    }
}
