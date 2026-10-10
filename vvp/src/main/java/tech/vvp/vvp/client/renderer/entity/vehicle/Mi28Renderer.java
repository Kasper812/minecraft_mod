package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.Mi28Model;
import tech.vvp.vvp.entity.vehicle.Mi28Entity;

public class Mi28Renderer extends VehicleRenderer<Mi28Entity> {
    public Mi28Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Mi28Model());
    }

    @Override
    public Identifier getTextureLocation(Mi28Entity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        
        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }
        
        return textures[0];
    }
}
