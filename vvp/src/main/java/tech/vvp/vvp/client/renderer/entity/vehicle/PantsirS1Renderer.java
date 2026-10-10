package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.PantsirS1Model;
import tech.vvp.vvp.entity.vehicle.PantsirS1Entity;

public class PantsirS1Renderer extends VehicleRenderer<PantsirS1Entity> {
    public PantsirS1Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PantsirS1Model());
    }

    @Override
    public Identifier getTextureLocation(PantsirS1Entity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
