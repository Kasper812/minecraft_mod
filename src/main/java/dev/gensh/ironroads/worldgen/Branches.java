package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.VillageSites;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Деревни этого мира: где они стоят и стоят ли вообще.
 *
 * <p>И раскладка, и отбор по биому спрашиваются у самого мира, а не считаются
 * константами: мод на структуры меняет и то и другое. Ответ кешируется по
 * региону — трасса спрашивает деревни на каждом отрезке, и повторная выборка
 * биома там была бы заметна.
 */
public final class Branches {
    private Branches() {}

    public static VillageSites sitesFor(ServerLevel level, ChunkGenerator generator) {
        StructureSet villages = villageSet(level);
        if (villages == null) {
            return VillageSites.EMPTY;
        }
        RandomSpreadStructurePlacement spread =
                villages.placement() instanceof RandomSpreadStructurePlacement placement
                ? placement : null;

        return new LevelSites(villages, spread, generator,
                              level.getChunkSource().randomState());
    }

    public static VillageSites obstaclesFor(ServerLevel level, ChunkGenerator generator) {
        List<StructureSet> sparse = new ArrayList<>();
        int cell = Integer.MAX_VALUE;

        for (StructureSet set : level.registryAccess()
                                     .registryOrThrow(Registries.STRUCTURE_SET)) {
            if (set.placement() instanceof RandomSpreadStructurePlacement placement
             && placement.spacing() >= MIN_OBSTACLE_SPACING) {
                sparse.add(set);
                cell = Math.min(cell, placement.spacing());
            }
        }
        if (sparse.isEmpty()) {
            return VillageSites.EMPTY;
        }
        return new Obstacles(sparse, cell, generator, level.getChunkSource().randomState());
    }

    private static final int MIN_OBSTACLE_SPACING = 16;

    private static boolean fitsBiome(StructureSet set, ChunkGenerator generator,
                                     RandomState randomState, int blockX, int blockZ) {
        Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(blockX),
                QuartPos.fromBlock(generator.getSeaLevel()),
                QuartPos.fromBlock(blockZ),
                randomState.sampler());

        for (StructureSet.StructureSelectionEntry entry : set.structures()) {
            if (entry.structure().value().biomes().contains(biome)) {
                return true;
            }
        }
        return false;
    }

    private static StructureSet villageSet(ServerLevel level) {
        return level.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE_SET)
                    .get(BuiltinStructureSets.VILLAGES);
    }

    private static final class LevelSites implements VillageSites {
        private final StructureSet villages;
        private final RandomSpreadStructurePlacement spread;
        private final ChunkGenerator generator;
        private final RandomState randomState;
        private final Map<Long, Long> cache = new ConcurrentHashMap<>();

        LevelSites(StructureSet villages, RandomSpreadStructurePlacement spread,
                   ChunkGenerator generator, RandomState randomState) {
            this.villages = villages;
            this.spread = spread;
            this.generator = generator;
            this.randomState = randomState;
        }

        @Override
        public int spacingChunks() {
            return spread == null ? VANILLA.spacingChunks() : spread.spacing();
        }

        @Override
        public long siteAt(long seed, int regionX, int regionZ) {
            long key = VillageSites.pack(regionX, regionZ);
            Long hit = cache.get(key);
            if (hit != null) {
                return hit;
            }
            long value = compute(seed, regionX, regionZ);
            cache.put(key, value);
            return value;
        }

        private long compute(long seed, int regionX, int regionZ) {
            // Позицию считает сама ванильная раскладка — с её spacing, salt и
            // spread_type, какими бы их ни сделал датапак. Концентрических
            // колец у деревень не бывает, но если placement окажется чужого
            // типа, ветки лягут по ванильной сетке вместо падения.
            long site = spread == null
                      ? VANILLA.siteAt(seed, regionX, regionZ)
                      : fromPlacement(seed, regionX, regionZ);

            return fitsBiome(villages, generator, randomState,
                             (int) (site >> 32), (int) site) ? site : NONE;
        }

        private long fromPlacement(long seed, int regionX, int regionZ) {
            // getPotentialStructureChunk сам делит на spacing, поэтому на вход
            // идёт любой чанк региона — берём его первый.
            ChunkPos pos = spread.getPotentialStructureChunk(
                    seed, regionX * spread.spacing(), regionZ * spread.spacing());
            return VillageSites.pack(pos.getMiddleBlockX(), pos.getMiddleBlockZ());
        }
    }

    private static final class Obstacles implements VillageSites {
        private final List<StructureSet> sets;
        private final int cellChunks;
        private final ChunkGenerator generator;
        private final RandomState randomState;
        private final Map<Long, Long> cache = new ConcurrentHashMap<>();

        Obstacles(List<StructureSet> sets, int cellChunks,
                  ChunkGenerator generator, RandomState randomState) {
            this.sets = sets;
            this.cellChunks = cellChunks;
            this.generator = generator;
            this.randomState = randomState;
        }

        @Override
        public int spacingChunks() {
            return cellChunks;
        }

        @Override
        public long siteAt(long seed, int regionX, int regionZ) {
            long key = VillageSites.pack(regionX, regionZ);
            Long hit = cache.get(key);
            if (hit != null) {
                return hit;
            }
            long value = compute(seed, regionX, regionZ);
            cache.put(key, value);
            return value;
        }

        private long compute(long seed, int regionX, int regionZ) {
            int minChunkX = regionX * cellChunks;
            int minChunkZ = regionZ * cellChunks;
            int centreX = (minChunkX + cellChunks / 2) * 16;
            int centreZ = (minChunkZ + cellChunks / 2) * 16;

            long best = NONE;
            long bestDistance = Long.MAX_VALUE;

            for (StructureSet set : sets) {
                RandomSpreadStructurePlacement placement =
                        (RandomSpreadStructurePlacement) set.placement();
                ChunkPos pos = placement.getPotentialStructureChunk(seed, minChunkX, minChunkZ);

                if (pos.x < minChunkX || pos.x >= minChunkX + cellChunks
                 || pos.z < minChunkZ || pos.z >= minChunkZ + cellChunks) {
                    continue;
                }
                int blockX = pos.getMiddleBlockX();
                int blockZ = pos.getMiddleBlockZ();
                long distance = (long) (blockX - centreX) * (blockX - centreX)
                              + (long) (blockZ - centreZ) * (blockZ - centreZ);

                if (distance < bestDistance
                 && fitsBiome(set, generator, randomState, blockX, blockZ)) {
                    best = VillageSites.pack(blockX, blockZ);
                    bestDistance = distance;
                }
            }
            return best;
        }
    }
}
