package com.atsuishio.superbwarfare.client.renderer

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.portlib.render.LegacyParticleRenderType
import com.atsuishio.portlib.render.SbmPipelines
import com.atsuishio.superbwarfare.Mod.Companion.loc
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.TextureAtlas

@Environment(EnvType.CLIENT)
object ModParticleRenderTypes {
    private val SOFT_TRANSLUCENT_PIPELINE: RenderPipeline = SbmPipelines.register(
        RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(loc("pipeline/particle_soft_translucent"))
            .withFragmentShader(loc("core/particle_soft"))
            .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(SbmPipelines.DEPTH_TEST_NO_WRITE)
            .build(),
        "PARTICLES_TRANSLUCENT"
    )

    @JvmStatic
    fun init() {
    }

    @JvmField
    val PARTICLE_SHEET_SOFT_TRANSLUCENT: LegacyParticleRenderType = LegacyParticleRenderType(
        "PARTICLE_SHEET_SOFT_TRANSLUCENT",
        LegacyParticleRenderType.sheet("superbwarfare_particle_soft_translucent", SOFT_TRANSLUCENT_PIPELINE, TextureAtlas.LOCATION_PARTICLES, true)
    )
}
