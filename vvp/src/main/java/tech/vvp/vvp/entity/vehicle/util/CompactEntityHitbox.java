package tech.vvp.vvp.entity.vehicle.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/** Visual height registry for HUD nametag / team overlay only. */
public final class CompactEntityHitbox {

    public static final float DEFAULT_VISUAL_HEIGHT = 3.5f;

    private static final Map<EntityType<?>, Float> VISUAL_HEIGHTS = new HashMap<>();

    private CompactEntityHitbox() {
    }

    public static void registerVisualHeight(EntityType<?> type, float height) {
        VISUAL_HEIGHTS.put(type, height);
    }

    public static float visualHeight(EntityType<?> type) {
        Float height = VISUAL_HEIGHTS.get(type);
        return height != null ? height : DEFAULT_VISUAL_HEIGHT;
    }

    public static float displayBbHeight(Entity entity) {
        if (entity instanceof ICompactEntityHitbox compact) {
            return compact.getVisualBbHeight();
        }
        return entity.getBbHeight();
    }
}
