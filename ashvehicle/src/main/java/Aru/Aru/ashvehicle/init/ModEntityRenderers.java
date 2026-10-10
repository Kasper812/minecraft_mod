package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.client.renderer.projectile.*;
import Aru.Aru.ashvehicle.client.renderer.entity.*;
import net.fabricmc.api.EnvType;

public class ModEntityRenderers {

    public static void registerEntityRenderers() {
        //weapons
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.AIM9.get(), Aim9Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.AIM120.get(), Aim120Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.AIM54.get(), Aim54Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.R60.get(), R60Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.AGM114.get(), Agm114Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.AGM158.get(), Agm158Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BALLISTIC_MISSILE.get(), BallisticMissileRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.TOILETBOMB.get(), ToiletBombRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.CBU87.get(), Cbu87Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.GBU57.get(), Gbu57Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.NUKE_BOMB.get(), NukeBombRenderer::new);

        // Register entity render for tom7
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.UH_60.get(), UH60Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_16.get(), F16Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_15.get(), F15Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MIG_15.get(), Mig15Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_4.get(), F4Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MIG_29.get(), Mig29Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.T_90.get(), T90Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.M1A1_ABRAMS.get(), M1A1AbramsRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.KV_2.get(), KV2Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.GEPARD_1A2.get(), GepardRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.SU_33.get(), SU33Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.M3A3_BRADLEY.get(), M3A3BradleyRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.SU_25.get(), SU25Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_39E.get(), F39ERenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.SU_34.get(), SU34Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_35B.get(),F35Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_35A.get(),F35ARenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.B_2.get(),B2Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_22.get(),F22Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MH_60M.get(), MH60MRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.SAPSAN_GRIM2.get(), SapsanRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_18.get(), F18Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_117.get(), F117Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.ZUMWALT.get(), ZumwaltRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.SU_57.get(), SU57Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.V_22.get(), V22Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_2.get(), F2Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.PANTSIR_S1.get(), pantsirS1Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.SU_27.get(), SU27Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.J_20.get(), J20Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EuroFighter.get(), EuroFighterRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.REAPER.get(), ReaperRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.YF_23.get(), YF23Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.X_47B.get(), X47BRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.RUBBER_BOAT.get(), RubberBoatRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.M_777.get(), m777Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.RAH_66.get(), Rah66Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.AH_64.get(), AH64Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.TOS.get(), TosRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.ZELENSKY.get(), ZelenskyRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.F_14.get(), F14Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.KA_52.get(), KA52Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.AC_130U.get(), Ac130uRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.B_52.get(), B52Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.C_130.get(), C130Renderer::new);
    }
}

