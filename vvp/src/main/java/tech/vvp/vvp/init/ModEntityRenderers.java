package tech.vvp.vvp.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import tech.vvp.vvp.VVP;
import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import tech.vvp.vvp.client.renderer.entity.vehicle.*;

public class ModEntityRenderers {
    public static void registerRenderers() {
        EntityRendererRegistry.register(ModEntities.BTR_4.get(), btr4Renderer::new);
        EntityRendererRegistry.register(ModEntities.BRADLEY.get(), BradleyRenderer::new);
        EntityRendererRegistry.register(ModEntities.TERMINATOR.get(), TerminatorRenderer::new);
        EntityRendererRegistry.register(ModEntities.T90_M.get(), T90MRenderer::new);
        EntityRendererRegistry.register(ModEntities.BRM.get(), BrmRenderer::new);
        EntityRendererRegistry.register(ModEntities.BMP_3.get(), Bmp3Renderer::new);
        EntityRendererRegistry.register(ModEntities.CHRYZANTEMA.get(), ChryzantemaRenderer::new);
        EntityRendererRegistry.register(ModEntities.M1A2.get(), M1A2Renderer::new);
        EntityRendererRegistry.register(ModEntities.PUMA.get(), PumaRenderer::new);
        EntityRendererRegistry.register(ModEntities.M1A2_SEP.get(), M1A2SepRenderer::new);
        EntityRendererRegistry.register(ModEntities.STRYKER.get(), StrykerRenderer::new);
        EntityRendererRegistry.register(ModEntities.STRYKER_M1296.get(), Stryker_M1296Renderer::new);
        EntityRendererRegistry.register(ModEntities.FMTV.get(), FMTVRenderer::new);
        EntityRendererRegistry.register(ModEntities.GAZ_TIGR.get(), GazTigrRenderer::new);
        EntityRendererRegistry.register(ModEntities.MI_28.get(), Mi28Renderer::new);
        EntityRendererRegistry.register(ModEntities.MI_24.get(), Mi24Renderer::new);
        EntityRendererRegistry.register(ModEntities.LEOPARD_2A7V.get(), Leopard2A7VRenderer::new);
        EntityRendererRegistry.register(ModEntities.LEOPARD_2A4.get(), Leopard2A4Renderer::new);
        EntityRendererRegistry.register(ModEntities.AH_64.get(), Ah64Renderer::new);
        EntityRendererRegistry.register(ModEntities.CHALLENGER.get(), ChallengerRenderer::new);
        EntityRendererRegistry.register(ModEntities.BMP_2.get(), Bmp2Renderer::new);
        EntityRendererRegistry.register(ModEntities.T72_B3M.get(), T72B3MRenderer::new);
        EntityRendererRegistry.register(ModEntities.BMP_2M.get(), Bmp2MRenderer::new);
        EntityRendererRegistry.register(ModEntities.URAL.get(), UralRenderer::new);
        EntityRendererRegistry.register(ModEntities.VARTA.get(), VartaRenderer::new);
        EntityRendererRegistry.register(ModEntities.PANTSIR_S1.get(), PantsirS1Renderer::new);
        EntityRendererRegistry.register(ModEntities.KORNET.get(), KornetRenderer::new);
        EntityRendererRegistry.register(ModEntities.AGS_30.get(), Ags30Renderer::new);
        EntityRendererRegistry.register(ModEntities.COBRA.get(), CobraRenderer::new);
        EntityRendererRegistry.register(ModEntities.CENTAURO.get(), CentauroRenderer::new);
        EntityRendererRegistry.register(ModEntities.BMPT_3K.get(), BMPT3KRenderer::new);
        EntityRendererRegistry.register(ModEntities.AJAX.get(), AjaxRenderer::new);
        EntityRendererRegistry.register(ModEntities.OPLOT.get(), OplotRenderer::new);
        EntityRendererRegistry.register(ModEntities.BTR_3.get(), Btr3Renderer::new);
        EntityRendererRegistry.register(ModEntities.PAUTINA.get(), PautinaRenderer::new);
        EntityRendererRegistry.register(ModEntities.CV_90.get(), CV90Renderer::new);

        EntityRendererRegistry.register(ModEntities.VARTA_PTRK.get(), VartaPTRKRenderer::new);
        EntityRendererRegistry.register(ModEntities.BMP_2_BAKHCHA.get(), Bmp2BakhcaRenderer::new);

        EntityRendererRegistry.register(ModEntities.PANTSIR_MISSILE.get(), BasicProjectileRenderer::new);

        // Mi-8 and D-30
        EntityRendererRegistry.register(ModEntities.MI_8.get(), Mi8Renderer::new);
        EntityRendererRegistry.register(ModEntities.D30.get(), D30Renderer::new);

        // NH-90
        EntityRendererRegistry.register(ModEntities.NH_90.get(), Nh90Renderer::new);
    }
}

