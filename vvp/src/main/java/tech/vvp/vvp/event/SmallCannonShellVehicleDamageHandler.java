package tech.vvp.vvp.event;

import com.atsuishio.superbwarfare.api.event.ProjectileHitEvent;
import com.atsuishio.superbwarfare.entity.projectile.SmallCannonShellEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import tech.vvp.vvp.VVP;

/**
 * {@code BypassesArmor} from vehicle weapon JSON is not applied to {@link SmallCannonShellEntity}.
 * Boost AP profile vs vehicles so 30 mm AP stays effective against armor.
 *
 * Runs on {@link ProjectileHitEvent.HitEntity}, which fires from {@code FastThrowableProjectile}
 * before {@link SmallCannonShellEntity} applies {@code forceHurt} / explosion damage.
 */
public final class SmallCannonShellVehicleDamageHandler {

    private static final float VEHICLE_AP_DAMAGE_MULTIPLIER = 1.15f;

    private SmallCannonShellVehicleDamageHandler() {
    }

    public static void onProjectileHitEntity(ProjectileHitEvent.HitEntity event) {
        if (!(event.getProjectile() instanceof SmallCannonShellEntity shell)) {
            return;
        }
        if (!(event.getTarget() instanceof VehicleEntity)) {
            return;
        }
        if (!isArmorPiercingProfile(shell)) {
            return;
        }

        shell.setDamageValue(shell.getDamageValue() * VEHICLE_AP_DAMAGE_MULTIPLIER);
        shell.setExplosionDamageValue(shell.getExplosionDamageValue() * VEHICLE_AP_DAMAGE_MULTIPLIER);
    }

    /**
     * AP shells emphasize direct damage over splash (e.g. Terminator AP 45/15 vs HE 15/22).
     */
    private static boolean isArmorPiercingProfile(SmallCannonShellEntity shell) {
        return shell.getDamageValue() > shell.getExplosionDamageValue();
    }
}
