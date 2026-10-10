package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.ChallengerModel;
import tech.vvp.vvp.entity.vehicle.ChallengerEntity;

public class ChallengerRenderer extends VehicleRenderer<ChallengerEntity> {
    public ChallengerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ChallengerModel());
    }

    @Override
    public Identifier getTextureLocation(ChallengerEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
