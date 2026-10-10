package tech.vvp.vvp.init;

import com.atsuishio.superbwarfare.item.container.ContainerBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import com.atsuishio.portlib.forge.DeferredRegister;
import com.atsuishio.portlib.forge.RegistryObject;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.init.ModItems;
import tech.vvp.vvp.init.ModEntities;

@SuppressWarnings("unused")
public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VVP.MOD_ID);

    // Общая вкладка со всеми предметами мода
    public static final RegistryObject<CreativeModeTab> NATO_VEHICLE_TAB = TABS.register("nato_tab", () -> net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.NATO_TAB_ICON.get()))
            .title(Component.translatable("natotab.vvp_natovehicle_tab"))
            .displayItems((parameters, output) -> {
                output.accept(ContainerBlockItem.createInstance(ModEntities.PUMA.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.BRADLEY.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.AJAX.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.CV_90.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.BTR_4.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.BTR_3.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.CENTAURO.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.STRYKER.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.STRYKER_M1296.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.M1A2_SEP.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.OPLOT.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.LEOPARD_2A7V.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.LEOPARD_2A4.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.VARTA.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.VARTA_PTRK.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.FMTV.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.AH_64.get()));
                output.accept(ContainerBlockItem.createInstance(ModEntities.COBRA.get()));

            })
            .build());

    public static final RegistryObject<CreativeModeTab> RU_VEHICLE_TAB = TABS.register("ru_tab",
            () -> net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab.builder()
                    .title(Component.translatable("rutab.vvp_ruvehicle_tab"))
                    .icon(() -> new ItemStack(ModItems.RU_TAB_ICON.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMP_2.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMP_2M.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMP_2_BAKHCHA.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMP_3.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BRM.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CHRYZANTEMA.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T72_B3M.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T90_M.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.TERMINATOR.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMPT_3K.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.URAL.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.PANTSIR_S1.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.PAUTINA.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.GAZ_TIGR.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.MI_28.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.MI_24.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.MI_8.get()));

                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> ARMOR_TAB = TABS.register("armor_tab",
                    () -> net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.vvp_armor_tab"))
                            .icon(() -> new ItemStack(ModItems.ARMOR_ICON.get()))
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.PANAMA.get());
                                output.accept(ModItems.KEPKA.get());
                                output.accept(ModItems.BERETA.get());
                                output.accept(ModItems.MI_28_HELMET.get());
                                output.accept(ModItems.MI_28_CHEST.get());
                                output.accept(ModItems.AGS_30_ITEM.get());
                                output.accept(ModItems.KORNET_ITEM.get());
                                output.accept(ModItems.ITEM_40_MM.get());
                                output.accept(ModItems.ITEM_30MM.get());
                                output.accept(ModItems.ITEM_7_62MM.get());
                                output.accept(ModItems.ITEM_12_7MM.get());
                                output.accept(ModItems.ITEM_AP_SHELL.get());
                                output.accept(ModItems.ITEM_HE_SHELL.get());
                                output.accept(ModItems.AGM.get());
                                output.accept(ModItems.AAM.get());
                                output.accept(ModItems.SPRAY.get());

                            })
                            .build());
}
