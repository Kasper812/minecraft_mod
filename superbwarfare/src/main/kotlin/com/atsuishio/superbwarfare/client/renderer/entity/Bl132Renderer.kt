package com.atsuishio.superbwarfare.client.renderer.entity

import com.atsuishio.superbwarfare.port.*

import net.minecraft.client.renderer.entity.EntityRendererProvider

class Bl132Renderer(manager: EntityRendererProvider.Context) : BasicArtilleryRenderer(manager) {
    override fun hideForTurretControllerWhileZooming(): Boolean {
        return true
    }
}
