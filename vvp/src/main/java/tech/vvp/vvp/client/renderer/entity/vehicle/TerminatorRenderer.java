package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.TerminatorModel;
import tech.vvp.vvp.entity.vehicle.TerminatorEntity;

public class TerminatorRenderer extends VehicleRenderer<TerminatorEntity> {
    public TerminatorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TerminatorModel());
    }

    @Override
    public Identifier getTextureLocation(TerminatorEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
