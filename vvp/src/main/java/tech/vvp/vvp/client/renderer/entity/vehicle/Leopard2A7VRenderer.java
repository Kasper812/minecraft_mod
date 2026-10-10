package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.Leopard2A7VModel;
import tech.vvp.vvp.entity.vehicle.Leopard2A7VEntity;

public class Leopard2A7VRenderer extends VehicleRenderer<Leopard2A7VEntity> {
    public Leopard2A7VRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Leopard2A7VModel());
    }

    @Override
    public Identifier getTextureLocation(Leopard2A7VEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
