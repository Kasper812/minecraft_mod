package dev.gensh.ironroads.geometry;

/**
 * Источник высоты поверхности. В игре за ним стоит ChunkGenerator,
 * в тестах — синтетический рельеф. Благодаря этому интерфейсу
 * TrackProfiler не знает про Minecraft.
 */
@FunctionalInterface
public interface HeightSampler {
    int heightAt(int x, int z);
}
