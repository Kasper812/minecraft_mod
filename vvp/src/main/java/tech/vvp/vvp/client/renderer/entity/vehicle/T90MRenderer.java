package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.T90MModel;
import tech.vvp.vvp.entity.vehicle.T90MEntity;

public class T90MRenderer extends VehicleRenderer<T90MEntity> {
    public T90MRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new T90MModel());
    }
}
