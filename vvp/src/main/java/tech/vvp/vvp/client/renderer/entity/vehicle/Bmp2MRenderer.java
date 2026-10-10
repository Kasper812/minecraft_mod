package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.Bmp2MModel;
import tech.vvp.vvp.entity.vehicle.Bmp2MEntity;

public class Bmp2MRenderer extends VehicleRenderer<Bmp2MEntity> {
    public Bmp2MRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Bmp2MModel());
    }
}
