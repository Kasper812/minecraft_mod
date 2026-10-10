package tech.vvp.vvp.entity.vehicle.util;

import com.atsuishio.superbwarfare.data.vehicle.subdata.SeatInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Seats with {@code Orientation} in JSON (NH90, MI-8 cargo, truck benches, etc.).
 * SBW render uses {@code yBodyRot}; this keeps body/head aligned with seat facing.
 */
public final class OrientedBenchSeats {

    private static final float HEAD_VEHICLE_ALIGN_EPS = 10f;

    private OrientedBenchSeats() {
    }

    public static void afterCopyEntityData(VehicleEntity vehicle, Entity entity) {
        int index = vehicle.getSeatIndex(entity);
        var seats = vehicle.computed().seats();
        if (index < 0 || index >= seats.size()) {
            return;
        }

        SeatInfo seat = seats.get(index);
        if (seat.getOrientation() == 0f) {
            return;
        }

        Vec3 oriented = vehicle.getTransformDirection(1f, entity);
        float orientedYaw = (float) -VehicleVecUtils.getYRotFromVector(oriented);

        entity.setYBodyRot(orientedYaw);

        if (!seat.getCanRotateHead()) {
            entity.setYRot(orientedYaw);
            entity.yRotO = orientedYaw;
            entity.setYHeadRot(orientedYaw);
            return;
        }

        float headVsVehicle = Mth.wrapDegrees(entity.getYRot() - vehicle.getYRot());
        if (Math.abs(headVsVehicle) < HEAD_VEHICLE_ALIGN_EPS) {
            entity.setYRot(orientedYaw);
            entity.yRotO = orientedYaw;
            entity.setYHeadRot(orientedYaw);
        }
    }
}
