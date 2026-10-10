package tech.vvp.vvp.event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import tech.vvp.vvp.entity.projectile.PantsirMissileEntity;

public class MissileSpawnHandler {
    
    public static void onMissileSpawn(Entity entity, ServerLevel level) {
        if (!(entity instanceof PantsirMissileEntity missile)) return;
        
        Vec3 velocity = missile.getDeltaMovement();
        if (velocity.lengthSqr() < 0.01) return;
        
        Vec3 direction = velocity.normalize();
        Vec3 currentPos = missile.position();
        Vec3 spawnPos = currentPos.subtract(direction.scale(0.5));
        
        missile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        missile.setInitialRotation(direction);
    }
}
