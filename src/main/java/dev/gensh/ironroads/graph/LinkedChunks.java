package dev.gensh.ironroads.graph;

import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Какие чанки уже прошли сшивание в текущем запуске сервера.
 *
 * ponytail: набор в памяти, не персистентный. Повторный проход после
 * перезапуска безвреден — TrackPropagator идемпотентен, а кривая не создаётся
 * дважды благодаря проверке существующих связей, — но стоит лишнего обхода
 * чанка. Если профилировщик покажет, что это заметно, переносить пометку в
 * SavedData, а не наращивать логику здесь.
 */
final class LinkedChunks {

    private static final int MAX_TRACKED = 20_000;

    private static final Set<ChunkPos> SEEN =
            Collections.synchronizedSet(new LinkedHashSet<>());

    static boolean contains(ChunkPos pos) {
        return SEEN.contains(pos);
    }

    static void add(ChunkPos pos) {
        synchronized (SEEN) {
            SEEN.add(pos);
            if (SEEN.size() > MAX_TRACKED) {
                Iterator<ChunkPos> oldest = SEEN.iterator();
                oldest.next();
                oldest.remove();
            }
        }
    }

    private LinkedChunks() {}
}
