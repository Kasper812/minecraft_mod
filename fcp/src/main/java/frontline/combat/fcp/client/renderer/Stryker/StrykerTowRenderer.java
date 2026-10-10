package frontline.combat.fcp.client.renderer.Stryker;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Stryker.StrykerTowModel;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerTowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class StrykerTowRenderer extends VehicleRenderer<StrykerTowEntity> {
    public StrykerTowRenderer(EntityRendererProvider.Context c) { super(c, new StrykerTowModel()); }
    @Override public Identifier getTextureLocation(StrykerTowEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
