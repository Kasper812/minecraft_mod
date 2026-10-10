package frontline.combat.fcp.compat;

import com.atsuishio.superbwarfare.init.ModCapabilities;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.CamoArtilleryBase;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import team.reborn.energy.api.EnergyStorage;

public final class FcpCapabilities {
    private FcpCapabilities() {
    }

    public static void register() {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!FCP.MODID.equals(id.getNamespace())) {
                continue;
            }
            ModCapabilities.ITEM_HANDLER_ENTITY.registerForType((entity, context) -> {
                if (entity instanceof CamoVehicleBase vehicle) {
                    return vehicle.itemHandlerForFcp();
                }
                if (entity instanceof CamoArtilleryBase artillery) {
                    return artillery.itemHandlerForFcp();
                }
                return null;
            }, type);
        }
    }

    public static int extractEnergy(Container container, int slot, int max) {
        if (max <= 0 || container.getItem(slot).isEmpty()) {
            return 0;
        }
        ContainerStorage storage = ContainerStorage.of(container, null);
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(storage.getSlot(slot));
        EnergyStorage source = context.find(EnergyStorage.ITEM);
        if (source == null || source.getAmount() <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = source.extract(max, transaction);
            transaction.commit();
            return (int) extracted;
        }
    }
}
