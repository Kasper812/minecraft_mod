package tech.vvp.vvp.client.renderer.armor;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import tech.vvp.vvp.item.armor.rus_helmet_2;

public class rus_helmet_2Renderer extends GeoArmorRenderer<rus_helmet_2> {
    public rus_helmet_2Renderer() {
        super(new rus_helmet_2Model());
    }

    @Override
    public RenderType getRenderType(rus_helmet_2 animatable, Identifier texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityTranslucent(getTextureLocation(animatable));
    }
}
