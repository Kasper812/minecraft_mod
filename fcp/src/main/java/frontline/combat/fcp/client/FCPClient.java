package frontline.combat.fcp.client;

import com.atsuishio.portlib.render.world.WorldRenderEvents;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.debug.VehicleDebugRenderer;
import frontline.combat.fcp.client.particle.ModParticles;
import frontline.combat.fcp.client.screen.FCPMenuScreens;
import frontline.combat.fcp.init.ModEntityRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.TypedEntityData;

@Environment(EnvType.CLIENT)
public class FCPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModEntityRenderers.registerRenderers();
        ModEntityRenderers.registerGuiOverlays();
        ModParticles.registerProviders();
        FCPKeybinds.onRegisterKeys();
        VehicleDebugRenderer.KeyReg.onRegisterKeys();
        FCPMenuScreens.onClientSetup();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            FCPKeybinds.onClientTick();
            EmplacementReloadKeyHandler.onClientTick();
            VehicleDebugRenderer.onClientTick();
        });
        WorldRenderEvents.AFTER_TRANSLUCENT.register(VehicleDebugRenderer::onRenderLevel);

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!(stack.getItem() instanceof BlockItem)) {
                return;
            }
            var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data == null) {
                return;
            }
            String entityType = data.copyTagWithoutId().getStringOr("EntityType", "");
            if (entityType.startsWith(FCP.MODID + ":vdv_")) {
                lines.add(Component.translatable("tooltip.fcp.usage_restriction").withStyle(ChatFormatting.RED));
            }
        });
    }
}
