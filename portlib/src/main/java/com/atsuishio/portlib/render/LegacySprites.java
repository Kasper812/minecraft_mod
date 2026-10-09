package com.atsuishio.portlib.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

public final class LegacySprites {
    private LegacySprites() {
    }

    public static TextureAtlasSprite block(Identifier id) {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(id);
    }

    public static TextureAtlasSprite atlas(Identifier atlas, Identifier id) {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(atlas).getSprite(id);
    }

    public static TextureAtlasSprite blockParticle(net.minecraft.world.level.block.state.BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
    }

    public static TextureAtlasSprite missing() {
        return block(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
    }
}
