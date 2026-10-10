package frontline.combat.fcp.event;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Detaches trailers whose driver is genuinely gone (killed, discarded, changed dimension). */
public class TrailerEventHandler {

    private static final double SEARCH_RADIUS = 32.0;

    public static void onLivingDeath(Entity entity) {
        detachTrailersFor(entity);
    }

    public static void onEntityLeaveLevel(Entity entity) {
        // Leaving the level also happens on plain chunk unload, and that driver is coming
        // back — detaching there would silently drop every hitch that crosses an unload
        // boundary or a world save. The trailer's own grace period handles a driver that
        // stays missing; only a real removal should hard-detach here.
        Entity.RemovalReason reason = entity.getRemovalReason();
        if (reason == Entity.RemovalReason.UNLOADED_TO_CHUNK
                || reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            return;
        }
        detachTrailersFor(entity);
    }

    private static void detachTrailersFor(Entity leaving) {
        Level level = leaving.level();
        if (level.isClientSide()) return;

        level.getEntitiesOfClass(
                AbstractTrailerEntity.class,
                leaving.getBoundingBox().inflate(SEARCH_RADIUS),
                trailer -> trailer.isDrivenBy(leaving)
        ).forEach(AbstractTrailerEntity::detach);
    }
}