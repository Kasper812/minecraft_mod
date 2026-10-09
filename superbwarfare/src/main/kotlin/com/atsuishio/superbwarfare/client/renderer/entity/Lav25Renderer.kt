package com.atsuishio.superbwarfare.client.renderer.entity

import com.atsuishio.superbwarfare.port.*

import net.minecraft.client.renderer.entity.EntityRendererProvider

class Lav25Renderer(manager: EntityRendererProvider.Context) : BasicVehicleRenderer(manager) {
    override fun hideForTurretControllerWhileZooming(): Boolean {
        return true
    }
}
