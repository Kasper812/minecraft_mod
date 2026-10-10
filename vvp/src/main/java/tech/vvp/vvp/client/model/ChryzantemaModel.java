package tech.vvp.vvp.client.model;

import tech.vvp.vvp.entity.vehicle.ChryzantemaEntity;

public class ChryzantemaModel extends VvpVehicleModel<ChryzantemaEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
