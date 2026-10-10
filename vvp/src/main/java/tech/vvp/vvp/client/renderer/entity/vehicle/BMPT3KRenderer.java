package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.BMPT3KModel;
import tech.vvp.vvp.entity.vehicle.BMPT3KEntity;

public class BMPT3KRenderer extends VehicleRenderer<BMPT3KEntity> {
    public BMPT3KRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BMPT3KModel());
    }

    @Override
    public Identifier getTextureLocation(BMPT3KEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
