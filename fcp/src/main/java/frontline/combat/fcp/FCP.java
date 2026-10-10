package frontline.combat.fcp;

import com.atsuishio.portlib.forge.event.EntityMountEvent;
import com.atsuishio.portlib.forge.event.PlayerInteractEvent;
import com.atsuishio.superbwarfare.api.event.ShootEvent;
import com.atsuishio.superbwarfare.api.event.SuperbWarfareEvents;
import com.mojang.logging.LogUtils;
import frontline.combat.fcp.event.MuzzleShootEventHandler;
import frontline.combat.fcp.event.TrailerEventHandler;
import frontline.combat.fcp.init.*;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.team.VehicleTeamEventHandler;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

public class FCP implements ModInitializer {
    public static final String MODID = "fcp";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final com.atsuishio.portlib.forge.network.SimpleChannel PACKET_HANDLER = FCPNetwork.FCP_HANDLER;

    @Override
    public void onInitialize() {
        ConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, FCPConfig.SPEC);

        ModSounds.REGISTRY.getEntries();
        ModParticleTypes.PARTICLE_TYPES.getEntries();
        ModEntities.ENTITY_TYPES.getEntries();
        ModItems.REGISTRY.getEntries();
        ModMenus.MENUS.getEntries();
        ModTabs.TABS.getEntries();
        FCPNetwork.register();
        frontline.combat.fcp.compat.FcpCapabilities.register();

        SuperbWarfareEvents.register(ShootEvent.Post.class, MuzzleShootEventHandler::onShootPost);

        PlayerInteractEvent.ENTITY_INTERACT.add(VehicleTeamEventHandler::onEntityInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(VehicleTeamEventHandler::onEntityInteractSpecific);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.vehicle.humvee.HumveeAttachmentHandler::onInteractSpecific);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Kamaz.KamazTentHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.ManHx60.ManHx60TentHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.ManHx58.ManHx58Mg3TentHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.ManHx58.ManHx58TentHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Fmtv.FMTVTentHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Ural.UralTentHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Uaz.UAZ3303TentHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Mi8.MI8MTVToggleHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Mi8.MI8AMTShToggleHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Mi8.MI8DoorGunsToggleHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Mi8.MI8ToggleHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdHeavyDoorHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdArmedDoorHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdDoorHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.M939.M939TentHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Fmtv4x4.FMTV4x4TentHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Oh1.Oh1ToggleHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Mi17.MI17DoorGunsToggleHandler::onInteract);
        PlayerInteractEvent.ENTITY_INTERACT_SPECIFIC.add(frontline.combat.fcp.entity.vehicle.Mi17.MI17ToggleHandler::onInteract);
        EntityMountEvent.LISTENERS.add(VehicleTeamEventHandler::onMount);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> VehicleTeamEventHandler.onServerAboutToStart());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> VehicleTeamEventHandler.onRegisterCommands(dispatcher));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> TrailerEventHandler.onLivingDeath(entity));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> TrailerEventHandler.onEntityLeaveLevel(entity));

        ResourceLoader loader = ResourceLoader.get(PackType.SERVER_DATA);
        Identifier sbwData = Identifier.fromNamespaceAndPath("superbwarfare", "server_data_loader");
        loader.registerReloadListener(loc("trailer_towed"), TrailerTowedConfigs.createReloadListener());
        loader.registerReloadListener(loc("trailer_driver"), TrailerDriverConfigs.createReloadListener());
        loader.registerReloadListener(loc("vehicle_data_reorganizer"), VehicleDataReorganizer.createReloadListener());
        loader.addListenerOrdering(sbwData, loc("vehicle_data_reorganizer"));
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
