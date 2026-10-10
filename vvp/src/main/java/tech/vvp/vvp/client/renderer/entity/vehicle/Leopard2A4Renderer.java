package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.Leopard2A4Model;
import tech.vvp.vvp.entity.vehicle.Leopard2A4Entity;

public class Leopard2A4Renderer extends VehicleRenderer<Leopard2A4Entity> {
    public Leopard2A4Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Leopard2A4Model());
    }

    @Override
    public Identifier getTextureLocation(Leopard2A4Entity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        
        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }
        
        return super.getTextureLocation(entity);
    }
}
