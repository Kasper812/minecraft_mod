package com.atsuishio.superbwarfare.client.renderer;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public final class ModRenderTypes {
    public static final Function<Identifier, RenderType> LASER = Util.memoize(location -> RenderType.create(
            "laser",
            RenderSetup.builder(ModPipelines.LASER)
                    .withTexture("Sampler0", location)
                    .useOverlay()
                    .affectsCrumbling()
                    .createRenderSetup()));

    public static final Function<Identifier, RenderType> LASER_BEAM = Util.memoize(location -> RenderType.create(
            "laser_beam",
            RenderSetup.builder(ModPipelines.LASER_BEAM)
                    .withTexture("Sampler0", location)
                    .useOverlay()
                    .affectsCrumbling()
                    .sortOnUpload()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()));

    public static final Function<Identifier, RenderType> ILLUMINATED = Util.memoize(location -> RenderType.create(
            "illuminated",
            RenderSetup.builder(ModPipelines.ILLUMINATED)
                    .withTexture("Sampler0", location)
                    .affectsCrumbling()
                    .createRenderSetup()));

    public static final Function<Identifier, RenderType> POLY_MESH_TRANSLUCENT_EMISSIVE = Util.memoize(location -> RenderType.create(
            "poly_mesh_translucent_emissive",
            RenderSetup.builder(ModPipelines.POLY_MESH_TRANSLUCENT_EMISSIVE)
                    .withTexture("Sampler0", location)
                    .sortOnUpload()
                    .createRenderSetup()));

    public static final Function<Identifier, RenderType> POLY_MESH_EYES = Util.memoize(location -> RenderType.create(
            "poly_mesh_eyes",
            RenderSetup.builder(ModPipelines.POLY_MESH_EYES)
                    .withTexture("Sampler0", location)
                    .sortOnUpload()
                    .createRenderSetup()));

    public static final Function<Identifier, RenderType> MUZZLE_FLASH_TYPE = Util.memoize(location -> RenderType.create(
            "muzzle_flash",
            RenderSetup.builder(ModPipelines.MUZZLE_FLASH)
                    .withTexture("Sampler0", location)
                    .sortOnUpload()
                    .createRenderSetup()));

    public static final Function<Identifier, RenderType> TOW_CHAIN = Util.memoize(location -> RenderType.create(
            "tow_chain",
            RenderSetup.builder(ModPipelines.TOW_CHAIN)
                    .withTexture("Sampler0", location)
                    .sortOnUpload()
                    .createRenderSetup()));

    public static final RenderType BLOCK_OVERLAY = RenderType.create(
            "block_overlay",
            RenderSetup.builder(ModPipelines.BLOCK_OVERLAY).createRenderSetup());

    private ModRenderTypes() {
    }

    public static RenderType laserBeam(Identifier location) {
        return LASER_BEAM.apply(location);
    }

    public static RenderType polyMeshEyes(Identifier location) {
        return POLY_MESH_EYES.apply(location);
    }
}
