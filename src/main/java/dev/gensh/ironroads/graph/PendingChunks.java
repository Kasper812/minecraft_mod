package dev.gensh.ironroads.graph;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class PendingChunks {
    private static final int MAX_TRACKED = 20_000;

    private static final Map<ResourceKey<Level>, PendingChunks> BY_LEVEL =
            new ConcurrentHashMap<>();

    /**
     * Насколько далеко дерево дотягивается через границы чанков. Ствол с кроной
     * укладывается в два чанка в каждую сторону с запасом, а больше и не нужно:
     * это радиус, в котором загрузка соседа может разблокировать чужую валку.
     */
    private static final int TREE_SPAN = 2;

    private final Queue<ChunkPos> pending = new ArrayDeque<>();
    private final Queue<ChunkPos> deferred = new ArrayDeque<>();
    private final Set<ChunkPos> seen = new HashSet<>();
    private final Map<ChunkPos, Integer> attempts = new HashMap<>();

    /**
     * Чанки, в которых уборка габарита не доделана: обход дерева упёрся в ещё не
     * загруженного соседа. Отдельно от {@link #pending} и от счётчика попыток
     * намеренно — сшивание путей имеет право сдаться по таймауту (иначе чанк узла
     * решётки не сошьётся никогда), а уборка не имеет: несваленное дерево стоит
     * поперёк пути навсегда и само не исчезнет. Ждём не по часам, а по событию,
     * которое единственное может помочь, — по загрузке соседа.
     */
    private final Set<ChunkPos> unswept = new HashSet<>();

    /**
     * Кто ждёт чью загрузку ради кривой: ключ — недостающий чанк, значение —
     * чанки, чьи разрывы без него не натянуть.
     *
     * Заведено по той же причине, что и {@link #unswept}, и от того же дефекта:
     * сшивание имеет право сдаться по таймауту, но не имеет права забыть о
     * пропущенной кривой. Раньше {@code forced} возвращал «готово» вместе с
     * пропуском, чанк уходил из очереди навсегда, и разрыв под кривую оставался
     * пустым до конца жизни мира — путь просто обрывался. Ждём не по часам, а по
     * загрузке именно того чанка, которого не хватило.
     */
    private final Map<ChunkPos, Set<ChunkPos>> waiting = new HashMap<>();

    static PendingChunks of(ResourceKey<Level> level) {
        return BY_LEVEL.computeIfAbsent(level, key -> new PendingChunks());
    }

    synchronized boolean offer(ChunkPos pos) {
        if (!seen.add(pos)) {
            return false;
        }
        if (seen.size() > MAX_TRACKED) {
            seen.clear();
            seen.add(pos);
        }
        pending.add(pos);
        return true;
    }

    synchronized void requeue(ChunkPos pos) {
        pending.add(pos);
    }

    synchronized void defer(ChunkPos pos) {
        deferred.add(pos);
        attempts.merge(pos, 1, Integer::sum);
    }

    synchronized int attempts(ChunkPos pos) {
        return attempts.getOrDefault(pos, 0);
    }

    synchronized void forget(ChunkPos pos) {
        attempts.remove(pos);
    }

    /**
     * Чанк выгрузился, пока стоял в очереди. Забываем и то, что он был виден:
     * иначе {@link #offer} никогда не пустит его обратно, и сшивание не
     * случится вовсе — чанк выгружается штатно, стоит игроку отойти.
     */
    synchronized void drop(ChunkPos pos) {
        seen.remove(pos);
        attempts.remove(pos);
    }

    /** Кривым этого чанка не хватило перечисленных чанков — ждём их загрузки. */
    synchronized void waitFor(ChunkPos pos, Set<ChunkPos> missing) {
        for (ChunkPos needed : missing) {
            waiting.computeIfAbsent(needed, key -> new HashSet<>()).add(pos);
        }
    }

    /** Пришёл чанк, которого ждали: вернуть в очередь всех, кому он был нужен. */
    synchronized void wakeWaiters(ChunkPos loaded) {
        Set<ChunkPos> woken = waiting.remove(loaded);
        if (woken != null) {
            pending.addAll(woken);
        }
    }

    /** Уборка габарита не доделана: ждём соседа, который её разблокирует. */
    synchronized void markUnswept(ChunkPos pos) {
        unswept.add(pos);
    }

    synchronized void markSwept(ChunkPos pos) {
        unswept.remove(pos);
    }

    /**
     * Разбудить соседей, которым не хватало именно этого чанка. Вызывается на
     * загрузку чанка: только она и может сдвинуть с места незаконченную валку,
     * поэтому опрашивать по таймеру нечего.
     */
    synchronized void wakeNeighbours(ChunkPos loaded) {
        if (unswept.isEmpty()) {
            return;
        }
        for (int dx = -TREE_SPAN; dx <= TREE_SPAN; dx++) {
            for (int dz = -TREE_SPAN; dz <= TREE_SPAN; dz++) {
                ChunkPos neighbour = new ChunkPos(loaded.x + dx, loaded.z + dz);
                if (unswept.remove(neighbour)) {
                    pending.add(neighbour);
                }
            }
        }
    }

    synchronized void promoteDeferred() {
        pending.addAll(deferred);
        deferred.clear();
    }

    synchronized ChunkPos poll() {
        return pending.poll();
    }

    private PendingChunks() {}
}
