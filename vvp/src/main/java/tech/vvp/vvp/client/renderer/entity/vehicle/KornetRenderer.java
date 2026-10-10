package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import tech.vvp.vvp.client.model.KornetModel;
import tech.vvp.vvp.entity.vehicle.KornetEntity;

public class KornetRenderer extends VehicleRenderer<KornetEntity> {

    public KornetRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KornetModel());
    }
}
