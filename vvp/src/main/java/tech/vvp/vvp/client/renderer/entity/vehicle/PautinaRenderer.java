package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.PautinaModel;
import tech.vvp.vvp.entity.vehicle.PautinaEntity;

public class PautinaRenderer extends VehicleRenderer<PautinaEntity> {
    public PautinaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PautinaModel());
    }
}
