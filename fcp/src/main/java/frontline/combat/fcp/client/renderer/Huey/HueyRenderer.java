package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.HueyModel;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class HueyRenderer extends VehicleRenderer<HueyEntity> {
    public HueyRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new HueyModel());}

    @Override
    public Identifier getTextureLocation(HueyEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
