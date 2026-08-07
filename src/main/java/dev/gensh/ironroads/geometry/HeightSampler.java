package dev.gensh.ironroads.geometry;

public interface HeightSampler {
    int heightAt(int x, int z);

    default int waterDepth(int x, int z) {
        return 0;
    }
}
