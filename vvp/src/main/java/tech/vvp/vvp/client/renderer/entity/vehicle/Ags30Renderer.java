package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.Ags30Model;
import tech.vvp.vvp.entity.vehicle.Ags30Entity;

public class Ags30Renderer extends VehicleRenderer<Ags30Entity> {
    public Ags30Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Ags30Model());
    }
}
