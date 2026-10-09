package com.atsuishio.superbwarfare.client.shader

object ThermalShaderHandler {
    private var isActive = false
    private var seeThroughWalls = false

    @JvmStatic
    fun register() {
    }

    @JvmStatic
    fun setSeeThroughWalls(seeThrough: Boolean) {
        seeThroughWalls = seeThrough
    }

    @JvmStatic
    fun setActive(active: Boolean) {
        isActive = active
    }

    @JvmStatic
    fun isActive(): Boolean {
        return isActive
    }
}
