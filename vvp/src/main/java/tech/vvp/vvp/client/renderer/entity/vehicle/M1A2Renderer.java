package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.M1A2Model;
import tech.vvp.vvp.entity.vehicle.M1A2Entity;

public class M1A2Renderer extends VehicleRenderer<M1A2Entity> {
    public M1A2Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new M1A2Model());
    }

    @Override
    public Identifier getTextureLocation(M1A2Entity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
