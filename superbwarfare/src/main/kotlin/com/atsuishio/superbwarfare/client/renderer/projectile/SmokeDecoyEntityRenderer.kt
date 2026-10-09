package com.atsuishio.superbwarfare.client.renderer.projectile

import com.atsuishio.portlib.render.legacy.LegacyEntityRenderer

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.Mod.Companion.loc
import com.atsuishio.superbwarfare.entity.projectile.SmokeDecoyEntity
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.Identifier

class SmokeDecoyEntityRenderer(pContext: EntityRendererProvider.Context) : LegacyEntityRenderer<SmokeDecoyEntity>(pContext) {
    override fun getTextureLocation(flareDecoy: SmokeDecoyEntity): Identifier {
        return TEXTURE
    }

    companion object {
        val TEXTURE = loc("textures/entity/empty.png")
    }
}
