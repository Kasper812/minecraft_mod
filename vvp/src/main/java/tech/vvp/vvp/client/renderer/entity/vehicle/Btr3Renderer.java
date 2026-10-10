package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.Btr3Model;
import tech.vvp.vvp.entity.vehicle.Btr3Entity;

public class Btr3Renderer extends VehicleRenderer<Btr3Entity> {
    public Btr3Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Btr3Model());
    }

    @Override
    public Identifier getTextureLocation(Btr3Entity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
