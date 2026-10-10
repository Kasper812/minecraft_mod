package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.VenomDoorGunsModel;
import frontline.combat.fcp.entity.vehicle.Huey.VenomDoorGunsEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class VenomDoorGunsRenderer extends VehicleRenderer<VenomDoorGunsEntity> {
    public VenomDoorGunsRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new VenomDoorGunsModel());}

    @Override
    public Identifier getTextureLocation(VenomDoorGunsEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
