package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.Stryker_M1296Model;
import tech.vvp.vvp.entity.vehicle.Stryker_M1296Entity;

public class Stryker_M1296Renderer extends VehicleRenderer<Stryker_M1296Entity> {
    public Stryker_M1296Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Stryker_M1296Model());
    }

    @Override
    public Identifier getTextureLocation(Stryker_M1296Entity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        
        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }
        
        return textures[0];
    }
}
