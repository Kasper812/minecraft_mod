package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.ChryzantemaModel;
import tech.vvp.vvp.entity.vehicle.ChryzantemaEntity;

public class ChryzantemaRenderer extends VehicleRenderer<ChryzantemaEntity> {
    public ChryzantemaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ChryzantemaModel());
    }
}
