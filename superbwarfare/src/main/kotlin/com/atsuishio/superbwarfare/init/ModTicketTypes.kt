package com.atsuishio.superbwarfare.init

import com.atsuishio.superbwarfare.Mod
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.TicketType

object ModTicketTypes {
    @JvmField
    val VEHICLE: TicketType = Registry.register(
        BuiltInRegistries.TICKET_TYPE,
        Mod.loc("vehicle"),
        TicketType(5L, TicketType.FLAG_LOADING or TicketType.FLAG_SIMULATION)
    )

    @JvmField
    val VEHICLE_START: TicketType = Registry.register(
        BuiltInRegistries.TICKET_TYPE,
        Mod.loc("vehicle_start"),
        TicketType(400L, TicketType.FLAG_LOADING or TicketType.FLAG_SIMULATION)
    )

    @JvmStatic
    fun init() {
    }
}
