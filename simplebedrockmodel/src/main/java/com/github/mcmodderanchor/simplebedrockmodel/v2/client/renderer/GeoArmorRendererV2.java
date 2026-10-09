package com.github.mcmodderanchor.simplebedrockmodel.v2.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.BedrockModelRenderTypes;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.IFPArmorHandRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.TreeArmorModelInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.tree.TreeBedrockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.atsuishio.portlib.render.SubmitBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 基于 Tree 模型的实例盔甲渲染器。
 */
public class GeoArmorRendererV2 implements IFPArmorHandRenderer {
    protected final TreeBedrockModel model;
    protected final TreeArmorModelInstance instance;
    private final EquipmentSlot armorSlot;
    private final Identifier texture;
    public boolean young;

    @Nullable
    protected ItemStack itemStack;
    @Nullable
    protected EquipmentSlot equipmentSlot;
    @Nullable
    protected HumanoidModel<?> original;

    public GeoArmorRendererV2(TreeBedrockModel model, EquipmentSlot armorSlot, Identifier texture) {
        this(model, new TreeArmorModelInstance(model), armorSlot, texture);
    }

    public GeoArmorRendererV2(TreeBedrockModel model, TreeArmorModelInstance instance, EquipmentSlot armorSlot, Identifier texture) {
        this.armorSlot = armorSlot;
        this.model = model;
        this.instance = instance;
        this.texture = texture;
    }

    public void preparePose(@Nullable LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        this.instance.preparePose(livingEntity, itemStack, equipmentSlot, original);
        this.itemStack = itemStack;
        this.equipmentSlot = equipmentSlot;
        this.original = original;
    }

    public void scaleModelForBaby(PoseStack poseStack, EquipmentSlot slot) {
        if (!this.young) {
            return;
        }
        if (slot == EquipmentSlot.HEAD) {
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(0.0F, 1.0F, 0.0F);
        } else {
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.0F, 1.5F, 0.0F);
        }
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, int color) {
        SubmitBufferSource buffers = new SubmitBufferSource(collector);
        renderArmorToBuffer(poseStack, buffers, light, overlay,
                ARGB.red(color) / 255.0F, ARGB.green(color) / 255.0F, ARGB.blue(color) / 255.0F, ARGB.alpha(color) / 255.0F);
        buffers.flush();
        afterRender();
    }

    public void renderArmorToBuffer(PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay,
                                    float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        if (this.equipmentSlot != null) {
            scaleModelForBaby(poseStack, this.equipmentSlot);
        }
        this.instance.renderToBuffer(
                poseStack,
                bufferSource,
                getRenderType(getTexture()),
                BedrockModelRenderTypes.polyMeshCutout(getTexture()),
                light,
                overlay,
                red,
                green,
                blue,
                alpha
        );
        poseStack.popPose();
    }

    public void afterRender() {
        this.itemStack = null;
        this.equipmentSlot = null;
        this.original = null;
    }

    @Override
    public void renderFirstPersonArmorArm(@NotNull AbstractClientPlayer player, @NotNull HumanoidArm arm, @NotNull PoseStack poseStack,
                                          @NotNull MultiBufferSource bufferSource, int packedLight) {
        BoneState armBone = arm == HumanoidArm.RIGHT
                ? this.instance.getArmorRightArm()
                : this.instance.getArmorLeftArm();
        if (armBone == null) {
            return;
        }

        this.instance.renderSingleBone(poseStack, armBone.index(), bufferSource, getRenderType(getTexture()),
                BedrockModelRenderTypes.polyMeshCutout(getTexture()), packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F, true);
    }

    public RenderType getRenderType(Identifier texture) {
        return RenderTypes.armorCutoutNoCull(texture);
    }

    public Identifier getTexture() {
        return this.texture;
    }

    public TreeBedrockModel getModel() {
        return this.model;
    }

    public TreeArmorModelInstance getInstance() {
        return this.instance;
    }

    public EquipmentSlot getArmorSlot() {
        return this.armorSlot;
    }

    @Nullable
    public EquipmentSlot getCurrentSlot() {
        return this.equipmentSlot;
    }
}
