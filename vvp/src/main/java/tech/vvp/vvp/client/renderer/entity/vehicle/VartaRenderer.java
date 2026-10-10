package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.VartaModel;
import tech.vvp.vvp.entity.vehicle.VartaEntity;

public class VartaRenderer extends VehicleRenderer<VartaEntity> {
    public VartaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VartaModel());
    }

    @Override
    public Identifier getTextureLocation(VartaEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
