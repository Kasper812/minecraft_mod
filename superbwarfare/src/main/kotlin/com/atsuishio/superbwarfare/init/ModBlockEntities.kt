package com.atsuishio.superbwarfare.init

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.Mod
import com.atsuishio.superbwarfare.block.entity.*
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.entity.BlockEntityType
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder

object ModBlockEntities {
    private fun <T : BlockEntityType<*>> registerBlockEntity(
        id: String,
        blockEntity: () -> T
    ): T {
        return Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Mod.MODID, id),
            blockEntity()
        )
    }

    @JvmField
    val CONTAINER = registerBlockEntity("container") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> ContainerBlockEntity(pos, state) },
            ModBlocks.CONTAINER
        ).build()
    }

    @JvmField
    val CHARGING_STATION = registerBlockEntity("charging_station") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> ChargingStationBlockEntity(pos, state) },
            ModBlocks.CHARGING_STATION
        ).build()
    }

    @JvmField
    val CREATIVE_CHARGING_STATION = registerBlockEntity("creative_charging_station") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> CreativeChargingStationBlockEntity(pos, state) },
            ModBlocks.CREATIVE_CHARGING_STATION
        ).build()
    }

    @JvmField
    val FUMO_25 = registerBlockEntity("fumo_25") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> FuMO25BlockEntity(pos, state) },
            ModBlocks.FUMO_25
        ).build()
    }

    @JvmField
    val SMALL_CONTAINER = registerBlockEntity("small_container") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> SmallContainerBlockEntity(pos, state) },
            ModBlocks.SMALL_CONTAINER
        ).build()
    }

    @JvmField
    val VEHICLE_DEPLOYER = registerBlockEntity("vehicle_deployer") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> VehicleDeployerBlockEntity(pos, state) },
            ModBlocks.VEHICLE_DEPLOYER
        ).build()
    }

    @JvmField
    val SUPERB_ITEM_INTERFACE = registerBlockEntity("superb_item_interface") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> SuperbItemInterfaceBlockEntity(pos, state) },
            ModBlocks.SUPERB_ITEM_INTERFACE
        ).build()
    }

    @JvmField
    val CREATIVE_SUPERB_ITEM_INTERFACE = registerBlockEntity("creative_superb_item_interface") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> CreativeSuperbItemInterfaceBlockEntity(pos, state) },
            ModBlocks.CREATIVE_SUPERB_ITEM_INTERFACE
        ).build()
    }

    @JvmField
    val LUCKY_CONTAINER = registerBlockEntity("lucky_container") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> LuckyContainerBlockEntity(pos, state) },
            ModBlocks.LUCKY_CONTAINER
        ).build()
    }

    @JvmField
    val VEHICLE_ASSEMBLING_TABLE = registerBlockEntity("vehicle_assembling_table") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> VehicleAssemblingTableBlockEntity(pos, state) },
            ModBlocks.VEHICLE_ASSEMBLING_TABLE
        ).build()
    }

    @JvmField
    val BLUEPRINT_RESEARCH_TABLE = registerBlockEntity("blueprint_research_table") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> BlueprintResearchTableBlockEntity(pos, state) },
            ModBlocks.BLUEPRINT_RESEARCH_TABLE
        ).build()
    }

    @JvmField
    val BIOGAS_GENERATOR = registerBlockEntity("biogas_generator") {
        FabricBlockEntityTypeBuilder.create(
            { pos, state -> BiogasGeneratorBlockEntity(pos, state) },
            ModBlocks.BIOGAS_GENERATOR
        ).build()
    }

    @JvmStatic
    fun init() {

    }
}
