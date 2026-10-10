package tech.vvp.vvp.client.model;

import tech.vvp.vvp.entity.vehicle.Stryker_M1296Entity;

public class Stryker_M1296Model extends VvpVehicleModel<Stryker_M1296Entity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
