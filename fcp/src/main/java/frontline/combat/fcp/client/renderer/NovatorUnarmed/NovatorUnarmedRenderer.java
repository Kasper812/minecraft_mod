package frontline.combat.fcp.client.renderer.NovatorUnarmed;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.NovatorUnarmed.NovatorUnarmedModel;
import frontline.combat.fcp.entity.vehicle.NovatorUnarmed.NovatorUnarmedEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class NovatorUnarmedRenderer extends VehicleRenderer<NovatorUnarmedEntity> {
    public NovatorUnarmedRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new NovatorUnarmedModel());}
    @Override public Identifier getTextureLocation(NovatorUnarmedEntity entity) {return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());}
}
