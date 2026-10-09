package com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderArmEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.IFPArmorHandRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 通用的第一人称盔甲手臂渲染处理器。
 * 监听 RenderArmEvent，在玩家手臂上叠加渲染 Bedrock 盔甲模型的手臂部分。
 */
@Environment(EnvType.CLIENT)
public class FirstPersonArmorHandler {
    private static final Map<Item, IFPArmorHandRenderer> RENDERERS = new IdentityHashMap<>();

    public static void register(ItemLike item, IFPArmorHandRenderer renderer) {
        RENDERERS.put(item.asItem(), renderer);
    }

    public static void onRenderArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        HumanoidArm arm = event.getArm();

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.isEmpty()) return;

        IFPArmorHandRenderer armorRenderer = RENDERERS.get(chestStack.getItem());
        if (armorRenderer == null) return;

        armorRenderer.renderFirstPersonArmorArm(
                player,
                arm,
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight()
        );
    }
}
