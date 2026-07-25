package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.HeightSampler;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Высота рельефа без генерации чанков. getBaseHeight считает шумовую функцию
 * ландшафта в произвольной точке — именно поэтому профиль пути может смотреть
 * на сотни блоков вперёд, не нарушая правило "фича трогает только свой чанк".
 */
public final class ChunkGeneratorHeightSampler implements HeightSampler {

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
        return generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                                       heightAccessor, randomState);
    }
}
