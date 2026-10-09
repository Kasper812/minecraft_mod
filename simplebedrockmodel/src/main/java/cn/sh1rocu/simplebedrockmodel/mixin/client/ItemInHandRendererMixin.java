package cn.sh1rocu.simplebedrockmodel.mixin.client;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderHandEvent;
import com.atsuishio.portlib.render.SubmitBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void sbm$renderHand(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack stack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        SubmitBufferSource buffers = new SubmitBufferSource(collector);
        var event = new RenderHandEvent(player, hand, stack, poseStack, collector, buffers, frameInterp, xRot, attack, inverseArmHeight, light);
        RenderHandEvent.EVENT.invoker().post(event);
        buffers.flush();
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
