package frontline.combat.fcp.client.model.Type10;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Abrams.M1a1TrackPaths;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
import frontline.combat.fcp.entity.vehicle.Type10.Type10Entity;
import net.minecraft.resources.Identifier;

public class Type10Model extends FCPVehicleModel<Type10Entity> {

    @Override
    public Identifier getModelResource(Type10Entity animatable) {
        return Identifier.fromNamespaceAndPath(FCP.MODID, "geo/type10.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    protected FCPTrackPath getTrackPath() {
        return Type10TrackPaths.TYPE10;
    }
}
