package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.StrykerModel;
import tech.vvp.vvp.entity.vehicle.StrykerEntity;

public class StrykerRenderer extends VehicleRenderer<StrykerEntity> {
    public StrykerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new StrykerModel());
    }

    @Override
    public Identifier getTextureLocation(StrykerEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        
        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }
        
        return textures[0];
    }
}
