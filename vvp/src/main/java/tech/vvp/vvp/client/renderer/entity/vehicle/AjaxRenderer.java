package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.AjaxModel;
import tech.vvp.vvp.entity.vehicle.AjaxEntity;

public class AjaxRenderer extends VehicleRenderer<AjaxEntity> {
    public AjaxRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AjaxModel());
    }

    @Override
    public Identifier getTextureLocation(AjaxEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
