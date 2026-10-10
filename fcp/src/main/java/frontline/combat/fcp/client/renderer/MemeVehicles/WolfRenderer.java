package frontline.combat.fcp.client.renderer.MemeVehicles;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.MemeVehicles.WolfModel;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.WolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class WolfRenderer extends VehicleRenderer<WolfEntity> {

    public WolfRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new WolfModel());}

    @Override
    public Identifier getTextureLocation(WolfEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
