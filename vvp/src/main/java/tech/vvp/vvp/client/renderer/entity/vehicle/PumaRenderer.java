package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.PumaModel;
import tech.vvp.vvp.entity.vehicle.PumaEntity;

public class PumaRenderer extends VehicleRenderer<PumaEntity> {
    
    public PumaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PumaModel());
    }

    @Override
    public Identifier getTextureLocation(PumaEntity entity) {
        // Получаем текстуры из entity
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        
        // Проверяем что индекс в пределах массива
        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }
        
        // Возвращаем первую текстуру по умолчанию
        return textures[0];
    }
}
