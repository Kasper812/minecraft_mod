package com.atsuishio.portlib.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class LegacyParticleRenderType {
    public static final LegacyParticleRenderType PARTICLE_SHEET_OPAQUE = new LegacyParticleRenderType("PARTICLE_SHEET_OPAQUE",
            sheet("legacy_particle_sheet_opaque", RenderPipelines.OPAQUE_PARTICLE, TextureAtlas.LOCATION_PARTICLES, false));
    public static final LegacyParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new LegacyParticleRenderType("PARTICLE_SHEET_TRANSLUCENT",
            sheet("legacy_particle_sheet_translucent", RenderPipelines.TRANSLUCENT_PARTICLE, TextureAtlas.LOCATION_PARTICLES, true));
    public static final LegacyParticleRenderType PARTICLE_SHEET_LIT = new LegacyParticleRenderType("PARTICLE_SHEET_LIT",
            sheet("legacy_particle_sheet_lit", RenderPipelines.OPAQUE_PARTICLE, TextureAtlas.LOCATION_PARTICLES, false));
    public static final LegacyParticleRenderType TERRAIN_SHEET = new LegacyParticleRenderType("TERRAIN_SHEET",
            sheet("legacy_particle_terrain_sheet", RenderPipelines.TRANSLUCENT_PARTICLE, TextureAtlas.LOCATION_BLOCKS, true));
    public static final LegacyParticleRenderType CUSTOM = new LegacyParticleRenderType("CUSTOM", null);
    public static final LegacyParticleRenderType NO_RENDER = new LegacyParticleRenderType("NO_RENDER", null);

    private final String name;
    private final @Nullable RenderType renderType;

    public LegacyParticleRenderType(String name, @Nullable RenderType renderType) {
        this.name = name;
        this.renderType = renderType;
    }

    public static RenderType sheet(String name, RenderPipeline pipeline, Identifier atlas, boolean translucent) {
        RenderSetup.RenderSetupBuilder builder = RenderSetup.builder(pipeline).withTexture("Sampler0", atlas).useLightmap();
        if (translucent) {
            builder.sortOnUpload();
        }
        return RenderType.create(name, builder.createRenderSetup());
    }

    public @Nullable RenderType renderType() {
        return renderType;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
