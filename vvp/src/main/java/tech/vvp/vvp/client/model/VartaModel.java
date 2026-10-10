package tech.vvp.vvp.client.model;

import tech.vvp.vvp.entity.vehicle.TerminatorEntity;
import tech.vvp.vvp.entity.vehicle.VartaEntity;

public class VartaModel extends VvpVehicleModel<VartaEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
