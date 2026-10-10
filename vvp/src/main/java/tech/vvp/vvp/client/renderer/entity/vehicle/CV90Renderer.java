package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.CV90Model;
import tech.vvp.vvp.entity.vehicle.CV90Entity;

public class CV90Renderer extends VehicleRenderer<CV90Entity> {
    public CV90Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CV90Model());
    }
}
