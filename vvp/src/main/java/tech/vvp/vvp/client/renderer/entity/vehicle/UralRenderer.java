package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.UralModel;
import tech.vvp.vvp.entity.vehicle.UralEntity;

public class UralRenderer extends VehicleRenderer<UralEntity> {
    public UralRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new UralModel());
    }
}
