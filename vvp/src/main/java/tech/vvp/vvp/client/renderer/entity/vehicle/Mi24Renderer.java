package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.Mi24Model;
import tech.vvp.vvp.entity.vehicle.Mi24Entity;

public class Mi24Renderer extends VehicleRenderer<Mi24Entity> {
    public Mi24Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Mi24Model());
    }

    @Override
    public Identifier getTextureLocation(Mi24Entity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        
        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }
        
        return textures[0];
    }
}
