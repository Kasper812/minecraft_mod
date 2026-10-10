package frontline.combat.fcp.client.renderer.GazTigr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GazTigr.GazTigrGLModel;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrGLEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class GazTigrGLRenderer extends VehicleRenderer<GazTigrGLEntity> {
    public GazTigrGLRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GazTigrGLModel());
    }

    @Override
    public Identifier getTextureLocation(GazTigrGLEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
