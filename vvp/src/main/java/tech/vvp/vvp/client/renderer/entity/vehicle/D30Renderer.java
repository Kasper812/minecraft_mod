package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.D30Model;
import tech.vvp.vvp.entity.vehicle.D30Entity;

public class D30Renderer extends VehicleRenderer<D30Entity> {

    public D30Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new D30Model());
    }
}
