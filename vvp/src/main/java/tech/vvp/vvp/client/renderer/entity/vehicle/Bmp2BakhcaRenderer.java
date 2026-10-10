package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.Bmp2BakhcaModel;
import tech.vvp.vvp.entity.vehicle.Bmp2BakhchaEntity;

public class Bmp2BakhcaRenderer extends VehicleRenderer<Bmp2BakhchaEntity> {
    public Bmp2BakhcaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Bmp2BakhcaModel());
    }

    @Override
    public Identifier getTextureLocation(Bmp2BakhchaEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
