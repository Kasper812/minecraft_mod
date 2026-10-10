package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.T72B3MModel;
import tech.vvp.vvp.entity.vehicle.T72B3MEntity;

public class T72B3MRenderer extends VehicleRenderer<T72B3MEntity> {
    public T72B3MRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new T72B3MModel());
    }

    @Override
    public Identifier getTextureLocation(T72B3MEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
