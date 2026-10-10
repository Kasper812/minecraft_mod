package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.GazTigrModel;
import tech.vvp.vvp.entity.vehicle.GazTigrEntity;

public class GazTigrRenderer extends VehicleRenderer<GazTigrEntity> {
    public GazTigrRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GazTigrModel());
    }
}
