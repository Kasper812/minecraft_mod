package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public abstract class LegacyTextureSheetParticle extends LegacyParticle {
    protected @Nullable TextureAtlasSprite sprite;
    protected float quadSize;
    protected float rCol = 1.0F;
    protected float gCol = 1.0F;
    protected float bCol = 1.0F;
    protected float alpha = 1.0F;
    protected float roll;
    protected float oRoll;

    protected LegacyTextureSheetParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    protected LegacyTextureSheetParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za) {
        super(level, x, y, z, xa, ya, za);
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
        return SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ;
    }

    public abstract LegacyParticleRenderType getRenderType();

    @Override
    public @Nullable RenderType getLegacyRenderType() {
        return getRenderType().renderType();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Quaternionf rotation = new Quaternionf();
        this.getFacingCameraMode().setRotation(rotation, camera, partialTicks);
        if (this.roll != 0.0F) {
            rotation.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }
        this.renderRotatedQuad(buffer, camera, rotation, partialTicks);
    }

    protected void renderRotatedQuad(VertexConsumer buffer, Camera camera, Quaternionf rotation, float partialTicks) {
        Vec3 pos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - pos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - pos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - pos.z());
        this.renderRotatedQuad(buffer, rotation, x, y, z, partialTicks);
    }

    protected void renderRotatedQuad(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float partialTicks) {
        float size = this.getQuadSize(partialTicks);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);
        this.renderVertex(buffer, rotation, x, y, z, 1.0F, -1.0F, size, u1, v1, light);
        this.renderVertex(buffer, rotation, x, y, z, 1.0F, 1.0F, size, u1, v0, light);
        this.renderVertex(buffer, rotation, x, y, z, -1.0F, 1.0F, size, u0, v0, light);
        this.renderVertex(buffer, rotation, x, y, z, -1.0F, -1.0F, size, u0, v1, light);
    }

    private void renderVertex(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float xOffset, float yOffset, float size, float u, float v, int light) {
        Vector3f vec = new Vector3f(xOffset, yOffset, 0.0F).rotate(rotation).mul(size).add(x, y, z);
        buffer.addVertex(vec.x(), vec.y(), vec.z()).setUv(u, v).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize;
    }

    public int getLightColor(float partialTick) {
        return this.getLightCoords(partialTick);
    }

    public LegacyTextureSheetParticle scale(float scale) {
        this.quadSize *= scale;
        super.scale(scale);
        return this;
    }

    public void setColor(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    protected void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    protected void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    protected float getU0() {
        return this.sprite == null ? 0.0F : this.sprite.getU0();
    }

    protected float getU1() {
        return this.sprite == null ? 1.0F : this.sprite.getU1();
    }

    protected float getV0() {
        return this.sprite == null ? 0.0F : this.sprite.getV0();
    }

    protected float getV1() {
        return this.sprite == null ? 1.0F : this.sprite.getV1();
    }

    public void pickSprite(SpriteSet spriteSet) {
        this.setSprite(spriteSet.get(this.random));
    }

    public void setSpriteFromAge(SpriteSet spriteSet) {
        if (!this.removed) {
            this.setSprite(spriteSet.get(this.age, this.lifetime));
        }
    }
}
