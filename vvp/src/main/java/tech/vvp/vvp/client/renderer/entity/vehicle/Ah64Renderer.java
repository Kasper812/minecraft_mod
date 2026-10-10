package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.Ah64Model;
import tech.vvp.vvp.entity.vehicle.Ah64Entity;

public class Ah64Renderer extends VehicleRenderer<Ah64Entity> {
    public Ah64Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Ah64Model());
    }
}
