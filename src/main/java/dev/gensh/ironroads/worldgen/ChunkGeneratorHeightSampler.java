package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.HeightSampler;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

public final class ChunkGeneratorHeightSampler implements HeightSampler {
    private static final int DEEP_WATER = 8;

    private static final int SHALLOW_WATER = 4;

    private final ChunkGenerator generator;
    private final LevelHeightAccessor heightAccessor;
    private final RandomState randomState;

    public ChunkGeneratorHeightSampler(ChunkGenerator generator,
                                       LevelHeightAccessor heightAccessor,
                                       RandomState randomState) {
        this.generator = generator;
        this.heightAccessor = heightAccessor;
        this.randomState = randomState;
    }

    @Override
    public int heightAt(int x, int z) {
        int terrain = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                                              heightAccessor, randomState);
        return Math.max(terrain, generator.getSeaLevel() + 1);
    }

    // Глубина воды берётся из биома, а не вторым getBaseHeight: второй проход по
    // шумовой колонке стоит столько же, сколько весь остальной профиль, а трассе
    // хватает знания «здесь большая вода». Спрашивается только в точках, где
    // маршрут выбирает обход, — не на общем пути выборки рельефа.
    @Override
    public int waterDepth(int x, int z) {
        Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(x),
                QuartPos.fromBlock(generator.getSeaLevel()),
                QuartPos.fromBlock(z),
                randomState.sampler());

        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN)) {
            return DEEP_WATER;
        }
        return biome.is(BiomeTags.IS_RIVER) ? SHALLOW_WATER : 0;
    }
}
