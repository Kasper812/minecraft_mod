package com.atsuishio.superbwarfare.compat.thermoo

import com.atsuishio.superbwarfare.port.*

import net.minecraft.server.level.ServerPlayer

object ThermooCompatHandler {
    @JvmStatic
    fun init() {
    }

    @JvmStatic
    fun onPlayerInVehicle(player: ServerPlayer) {
    }

    @JvmStatic
    fun hasMod(): Boolean = false
}
