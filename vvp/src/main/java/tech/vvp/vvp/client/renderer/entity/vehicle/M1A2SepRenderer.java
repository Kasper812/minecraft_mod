package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.M1A2SepModel;
import tech.vvp.vvp.entity.vehicle.M1A2SepEntity;

public class M1A2SepRenderer extends VehicleRenderer<M1A2SepEntity> {
    public M1A2SepRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new M1A2SepModel());
    }
}
