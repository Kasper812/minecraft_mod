package tech.vvp.vvp;

import com.atsuishio.portlib.forge.network.SimpleChannel;
import com.atsuishio.superbwarfare.api.event.ProjectileHitEvent;
import com.atsuishio.superbwarfare.api.event.ShootEvent;
import com.atsuishio.superbwarfare.api.event.SuperbWarfareEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.event.MissileSpawnHandler;
import tech.vvp.vvp.event.MuzzleShootEventHandler;
import tech.vvp.vvp.event.SmallCannonShellVehicleDamageHandler;
import tech.vvp.vvp.init.*;
import tech.vvp.vvp.network.VVPNetwork;

public class VVP implements ModInitializer {
    public static final String MOD_ID = "vvp";
    public static final SimpleChannel PACKET_HANDLER = VVPNetwork.VVP_HANDLER;

    @Override
    public void onInitialize() {
        ModSounds.REGISTRY.getEntries();
        ModParticleTypes.PARTICLE_TYPES.getEntries();
        ModEntities.ENTITY_TYPES.getEntries();
        ModItems.REGISTRY.getEntries();
        ModTabs.TABS.getEntries();
        VVPNetwork.register();

        SuperbWarfareEvents.register(ShootEvent.Post.class, MuzzleShootEventHandler::onShootPost);
        SuperbWarfareEvents.register(ProjectileHitEvent.HitEntity.class, SmallCannonShellVehicleDamageHandler::onProjectileHitEntity);
        ServerEntityEvents.ENTITY_LOAD.register(MissileSpawnHandler::onMissileSpawn);
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
