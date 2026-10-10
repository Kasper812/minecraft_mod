package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.overlay.FcpPilotOverlay;
import frontline.combat.fcp.client.renderer.Aavp.AAVPRenderer;
import frontline.combat.fcp.client.renderer.Abrams.M1a1Renderer;
import frontline.combat.fcp.client.renderer.Abrams.M1a2Renderer;
import frontline.combat.fcp.client.renderer.Bmp.Bmp1.BMP1AMRenderer;
import frontline.combat.fcp.client.renderer.Bmp.Bmp1.BMP1Renderer;
import frontline.combat.fcp.client.renderer.Bmp.Bmp1.BMP1URenderer;
import frontline.combat.fcp.client.renderer.Bmp.Bmp2.BMP2Renderer;
import frontline.combat.fcp.client.renderer.Bmp.Bmp1.BMP1PRenderer;
import frontline.combat.fcp.client.renderer.Bmp.Bmp2.BMP2DRenderer;
import frontline.combat.fcp.client.renderer.Bmp.Bmp2.BMP2MRenderer;
import frontline.combat.fcp.client.renderer.Bmp.Bmp2.BMP2NoAtgmRenderer;
import frontline.combat.fcp.client.renderer.Bmp.Bmp2.BMP2MDRenderer;
import frontline.combat.fcp.client.renderer.Btr.BTR82Renderer;
import frontline.combat.fcp.client.renderer.Btr.BTR82ATRenderer;
import frontline.combat.fcp.client.renderer.Btr.BTR3ERenderer;
import frontline.combat.fcp.client.renderer.Btr.BTR4MV1Renderer;
import frontline.combat.fcp.client.renderer.Btr.BTR80Renderer;
import frontline.combat.fcp.client.renderer.Btr.BTR80CopeRenderer;
import frontline.combat.fcp.client.renderer.Btr.BTR82CopeRenderer;
import frontline.combat.fcp.client.renderer.Fmtv.FMTVRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrGLRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrDualRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrMGRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrRWSRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrRenderer;
import frontline.combat.fcp.client.renderer.Huey.HueyRenderer;
import frontline.combat.fcp.client.renderer.Huey.HueyM60DoorGunsRenderer;
import frontline.combat.fcp.client.renderer.Huey.HueyM60GunshipRenderer;
import frontline.combat.fcp.client.renderer.Huey.HueyM134DoorGunsRenderer;
import frontline.combat.fcp.client.renderer.Huey.HueyM134GunshipRenderer;
import frontline.combat.fcp.client.renderer.Huey.VenomRenderer;
import frontline.combat.fcp.client.renderer.Huey.VenomGunshipRenderer;
import frontline.combat.fcp.client.renderer.Huey.VenomDoorGunsRenderer;
import frontline.combat.fcp.client.renderer.JohnDeere.CombineRenderer;
import frontline.combat.fcp.client.renderer.JohnDeere.CultivatorRenderer;
import frontline.combat.fcp.client.renderer.Humvee.HumveeUnarmedRenderer;
import frontline.combat.fcp.client.renderer.JohnDeere.JohnDeereRenderer;
import frontline.combat.fcp.client.renderer.JohnDeere.SeederRenderer;
import frontline.combat.fcp.client.renderer.Kamaz.KamazRenderer;
import frontline.combat.fcp.client.renderer.Lav.Lav25Renderer;
import frontline.combat.fcp.client.renderer.Littlebird.LittlebirdHeavyRenderer;
import frontline.combat.fcp.client.renderer.Littlebird.LittlebirdArmedRenderer;
import frontline.combat.fcp.client.renderer.Littlebird.LittlebirdRenderer;
import frontline.combat.fcp.client.renderer.M109.M109Renderer;
import frontline.combat.fcp.client.renderer.Matv.MATV9In1Renderer;
import frontline.combat.fcp.client.renderer.Matv.MATVCrowsRenderer;
import frontline.combat.fcp.client.renderer.Matv.MATVRenderer;
import frontline.combat.fcp.client.renderer.Matv.MATVTOWRenderer;
import frontline.combat.fcp.client.renderer.Rg33.RG33Renderer;
import frontline.combat.fcp.client.renderer.MemeVehicles.BigBirdRenderer;
import frontline.combat.fcp.client.renderer.MemeVehicles.WolfRenderer;
import frontline.combat.fcp.client.renderer.Mi17.MI17Renderer;
import frontline.combat.fcp.client.renderer.Mi17.MI17DoorGunsRenderer;
import frontline.combat.fcp.client.renderer.Mi8.MI8Renderer;
import frontline.combat.fcp.client.renderer.Mi8.MI8DoorGunsRenderer;
import frontline.combat.fcp.client.renderer.Mi8.MI8MTVRenderer;
import frontline.combat.fcp.client.renderer.Mi8.MI8AMTShRenderer;
import frontline.combat.fcp.client.renderer.Msta.MstaRenderer;
import frontline.combat.fcp.client.renderer.Novator.NovatorRenderer;
import frontline.combat.fcp.client.renderer.NovatorUnarmed.NovatorUnarmedRenderer;
import frontline.combat.fcp.client.renderer.Dpv.DpvM240Renderer;
import frontline.combat.fcp.client.renderer.Dpv.DpvMinigunRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Hellfire.WireGuidedHellfireRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Malyutka.MalyutkaRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Sidewinder.SidewinderRenderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerM2Renderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerMGSRenderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerDragoonRenderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerMk19Renderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerTowRenderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerMortarRenderer;
import frontline.combat.fcp.client.renderer.T72av.T72AVRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxBMPRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxRocketPodRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxSpg9Renderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxMortarRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxZu23Renderer;
import frontline.combat.fcp.client.renderer.Trailers.ExampleTrailer.ExampleTrailerRenderer;
import frontline.combat.fcp.client.renderer.Type10.Type10Renderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZDSHKARenderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZSPG9Renderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZRenderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZ452Renderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZ3303Renderer;
import frontline.combat.fcp.client.renderer.Ural.UralGradRenderer;
import frontline.combat.fcp.client.renderer.Ural.UralRenderer;
import frontline.combat.fcp.client.renderer.Viper.ViperRenderer;
import frontline.combat.fcp.client.renderer.Oh1.Oh1Renderer;
import frontline.combat.fcp.client.renderer.Ch53.Ch53aRenderer;
import frontline.combat.fcp.client.renderer.Ch53.Ch53eRenderer;
import frontline.combat.fcp.client.renderer.Ch53.Mh53Renderer;
import frontline.combat.fcp.client.renderer.Uh60.Uh60Renderer;
import frontline.combat.fcp.client.renderer.Uh60.Uh60MinigunRenderer;
import frontline.combat.fcp.client.renderer.Uh60.Mh60lRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Hellfire.LockOnHellfireRenderer;
import net.fabricmc.api.EnvType;

import frontline.combat.fcp.client.overlay.FcpDriverOverlay;

public class ModEntityRenderers {
    public static void registerRenderers() {
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.TOYOTA_HILUX.get(), ToyotaHiluxRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.DPV_M240.get(), DpvM240Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.DPV_MINIGUN.get(), DpvMinigunRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.TOYOTA_HILUX_ROCKET_POD.get(), ToyotaHiluxRocketPodRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.TOYOTA_HILUX_BMP.get(), ToyotaHiluxBMPRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.TOYOTA_HILUX_SPG9.get(), ToyotaHiluxSpg9Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.TOYOTA_HILUX_MORTAR.get(), ToyotaHiluxMortarRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.TOYOTA_HILUX_ZU23.get(), ToyotaHiluxZu23Renderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.UAZ.get(), UAZRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.UAZ_DSHKA.get(), UAZDSHKARenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.UAZ_SPG9.get(), UAZSPG9Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.UAZ_452.get(), UAZ452Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.UAZ_3303.get(), UAZ3303Renderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.STRYKER_MGS.get(), StrykerMGSRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.STRYKER_M2.get(), StrykerM2Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.STRYKER_DRAGOON.get(), StrykerDragoonRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.STRYKER_MK19.get(), StrykerMk19Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.STRYKER_TOW.get(), StrykerTowRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.STRYKER_MORTAR.get(), StrykerMortarRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.LITTLEBIRD.get(), LittlebirdRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.LITTLEBIRD_ARMED.get(), LittlebirdArmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.LITTLEBIRD_HEAVY.get(), LittlebirdHeavyRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BMP1.get(), BMP1Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BMP1U.get(), BMP1URenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BMP1AM.get(), BMP1AMRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BMP2.get(), BMP2Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BMP1P.get(), BMP1PRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BMP2D.get(), BMP2DRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BMP2M.get(), BMP2MRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BMP2_NOATGM.get(), BMP2NoAtgmRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BMP2MD.get(), BMP2MDRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.AAVP.get(), AAVPRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.LAV25.get(), Lav25Renderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.T72AV.get(), T72AVRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.URAL.get(), UralRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.URAL_FUEL.get(), frontline.combat.fcp.client.renderer.Ural.UralFuelRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.URAL_KUNG.get(), frontline.combat.fcp.client.renderer.Ural.UralKungRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.URAL_GRAD.get(), UralGradRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.KAMAZ.get(), KamazRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.KAMAZ_KUNG.get(), frontline.combat.fcp.client.renderer.Kamaz.KamazKungRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.KAMAZ_LONG.get(), frontline.combat.fcp.client.renderer.Kamaz.KamazLongRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.VIPER.get(), ViperRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.OH1.get(), Oh1Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.CH53A.get(), Ch53aRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.CH53E.get(), Ch53eRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MH53.get(), Mh53Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.UH60.get(), Uh60Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.UH60_MINIGUN.get(), Uh60MinigunRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MH60L.get(), Mh60lRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.GAZ_TIGR.get(), GazTigrRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.GAZ_TIGR_RWS.get(), GazTigrRWSRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.GAZ_TIGR_MG.get(), GazTigrMGRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.GAZ_TIGR_GL.get(), GazTigrGLRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.GAZ_TIGR_DUAL.get(), GazTigrDualRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HUEY.get(), HueyRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HUEY_M60_DOOR_GUNS.get(), HueyM60DoorGunsRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HUEY_M60_GUNSHIP.get(), HueyM60GunshipRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HUEY_M134_DOOR_GUNS.get(), HueyM134DoorGunsRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HUEY_M134_GUNSHIP.get(), HueyM134GunshipRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.VENOM.get(), VenomRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.VENOM_GUNSHIP.get(), VenomGunshipRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.VENOM_DOOR_GUNS.get(), VenomDoorGunsRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.NOVATOR.get(), NovatorRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.NOVATOR_UNARMED.get(), NovatorUnarmedRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MATV.get(), MATVRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.RG_33.get(), RG33Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EMPL_TOW.get(), frontline.combat.fcp.client.renderer.Emplacement.EmplTowRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EMPL_KORNET.get(), frontline.combat.fcp.client.renderer.Emplacement.EmplKornetRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EMPL_DSHK.get(), frontline.combat.fcp.client.renderer.Emplacement.EmplDshkRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EMPL_MK19.get(), frontline.combat.fcp.client.renderer.Emplacement.EmplMk19Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EMPL_AGS17.get(), frontline.combat.fcp.client.renderer.Emplacement.EmplAgs17Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EMPL_M2.get(), frontline.combat.fcp.client.renderer.Emplacement.EmplM2Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EMPL_MG3.get(), frontline.combat.fcp.client.renderer.Emplacement.EmplMg3Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EMPL_ZIS3.get(), frontline.combat.fcp.client.renderer.Emplacement.EmplZis3Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.PANTSIR.get(), frontline.combat.fcp.client.renderer.Pantsir.PantsirRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BRDM2.get(), frontline.combat.fcp.client.renderer.Brdm.Brdm2Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.M939.get(), frontline.combat.fcp.client.renderer.M939.M939Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MAN_HX58.get(), frontline.combat.fcp.client.renderer.ManHx58.ManHx58Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MAN_HX58_MG3.get(), frontline.combat.fcp.client.renderer.ManHx58.ManHx58Mg3Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MAN_HX60.get(), frontline.combat.fcp.client.renderer.ManHx60.ManHx60Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.GENERIC_SUV.get(), frontline.combat.fcp.client.renderer.GenericSuv.GenericSuvRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.FMTV_4X4.get(), frontline.combat.fcp.client.renderer.Fmtv4x4.FMTV4x4Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.KOZAK5.get(), frontline.combat.fcp.client.renderer.Kozak.Kozak5Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.KOZAK2M1.get(), frontline.combat.fcp.client.renderer.Kozak.Kozak2m1Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.KOZAK_AMBULANCE.get(), frontline.combat.fcp.client.renderer.Kozak.KozakAmbulanceRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MATV_TOW.get(), MATVTOWRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MATV_CROW.get(), MATVCrowsRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MATV_9IN1.get(), MATV9In1Renderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_AMBULANCE.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_ARMORED_M2.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_ARMORED_MK19.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_ARMORED_UNARMED.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_ASRAD.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_AVENGER.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_CARGO.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_SHELTER.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_SOFT_TOP.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_SOFT_TOP_NO_DOORS.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_UNARMORED_M2.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_UNARMORED_M2_SHIELD.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_UNARMORED_M2_TURRET.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_UNARMORED_TOW.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_UNARMORED_TOW_TURRET.get(), HumveeUnarmedRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HMMWV_UNARMORED_UNARMED.get(), HumveeUnarmedRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BTR82.get(), BTR82Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BTR82AT.get(), BTR82ATRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BTR3E.get(), BTR3ERenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BTR4MV1.get(), BTR4MV1Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BTR80.get(), BTR80Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BTR80_COPE.get(), BTR80CopeRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BTR82_COPE.get(), BTR82CopeRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MI17.get(), MI17Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MI8.get(), MI8Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MI17_DOOR_GUNS.get(), MI17DoorGunsRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MI8_DOOR_GUNS.get(), MI8DoorGunsRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MI8_MTV.get(), MI8MTVRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MI8_AMTSH.get(), MI8AMTShRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.FMTV.get(), FMTVRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.M109.get(), M109Renderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MSTA.get(), MstaRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.JOHN_DEERE.get(), JohnDeereRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.COMBINE.get(), CombineRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.M1A1.get(), M1a1Renderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.M1A2.get(), M1a2Renderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.TYPE10.get(), Type10Renderer::new);

        // Projectiles
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.LOCK_ON_HELLFIRE.get(), LockOnHellfireRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.WIRE_GUIDED_HELLFIRE.get(), WireGuidedHellfireRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.SIDEWINDER.get(), SidewinderRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.MALYUTKA.get(), MalyutkaRenderer::new);

        // Meme Vehicles
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.BIGBIRD.get(), BigBirdRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.T14_ARMATA.get(), WolfRenderer::new);
        // Trailers
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.EXAMPLE_TRAILER.get(), ExampleTrailerRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.SEEDER.get(), SeederRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.CULTIVATOR.get(), CultivatorRenderer::new);
    }
    public static void registerGuiOverlays() {
        com.atsuishio.portlib.forge.client.GuiOverlays.registerBelowAll(frontline.combat.fcp.FCP.loc(FcpDriverOverlay.ID), FcpDriverOverlay.INSTANCE);
        com.atsuishio.portlib.forge.client.GuiOverlays.registerBelowAll(frontline.combat.fcp.FCP.loc(FcpPilotOverlay.ID), FcpPilotOverlay.INSTANCE);
    }

}

