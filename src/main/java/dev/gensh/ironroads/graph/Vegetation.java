package dev.gensh.ironroads.graph;

import dev.gensh.ironroads.IronRoads;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Валка дерева целиком: ствол, корни, крона и всё, что на ней висит.
 *
 * Два правила, из которых вырастает весь класс.
 *
 * <b>Либо всё дерево, либо ни одного блока.</b> Дерево сначала собирается в
 * множество и только потом стирается. Прошлая версия стирала по ходу обхода и
 * останавливалась по бюджету — от этого в небе оставались кроны без стволов.
 * Здесь исчерпанный бюджет или незагруженный сосед означают, что не тронуто
 * ничего.
 *
 * <b>Соседнее дерево — не наше.</b> Обход по сомкнутым кронам в тёмном дубовом
 * лесу свалил бы рощу целиком, поэтому разлив идёт по древесине, а крона
 * добирается от неё отдельно, с проверкой по {@link LeavesBlock#DISTANCE}. Это
 * свойство самой листвы: в нём записано расстояние до ближайшей древесины. Если
 * оно меньше, чем число шагов, за которое мы до этого листа дошли, значит ближе
 * есть чужой ствол — лист останется на нём и в воздухе не повиснет. Проверка
 * стоит одно чтение блока и заменяет любую эвристику по радиусу.
 */
final class Vegetation {
    /**
     * Предел на дерево. Гигантская тропическая сосна с кроной укладывается в
     * полторы тысячи блоков, тёмный дуб — в шестьсот, так что запас четырёхкратный
     * и в ваниле не достигается. Модовое дерево крупнее не валим вовсе: ветка над
     * путём — это некрасиво, а полдерева — это сломано.
     */
    private static final int BUDGET = 4096;

    /** Дальше этого крона от своей древесины не уходит: предел DISTANCE в ваниле. */
    private static final int LEAF_REACH = 7;

    /**
     * Незагруженный сосед лечится повтором, бюджет — нет, и различать их
     * обязательно: если просить повтор из-за бюджета, чанк будет вставать в
     * очередь и заново обходить то же дерево каждый тик до самого FORCE_AFTER.
     */
    private enum Result { DONE, UNLOADED, TOO_BIG }

    private Vegetation() {}

    /** Дерево: то, что нельзя убрать по блоку, не оставив висеть остальное. */
    static boolean isTree(BlockState state) {
        return isWood(state) || isCanopy(state);
    }

    /**
     * Прочая растительность: трава, цветы, саженцы, посевы, лианы, бамбук,
     * кактус, тростник, ягодник. У всей у неё одна общая черта, из-за которой она
     * и попадает в отдельную ветку: она стоит колонкой и держится на блоке под
     * собой, поэтому убирается снизу вверх и обходов не требует.
     *
     * Проверка по тегам, а не по списку блоков: модовая трава и модовые саженцы
     * лежат в тех же ванильных тегах и попадают сюда сами. Бамбук с кактусом и
     * тростником названы прямо — общего тега на них в ваниле нет.
     */
    static boolean isPlant(BlockState state) {
        // Вода лежит в REPLACEABLE_BY_TREES (проверено по самому тегу, не по
        // памяти). Без этой строчки pull() принимал бы её за растение и уходил
        // столбом вверх до границы мира, прорезая озеро над путём насквозь.
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        return state.is(BlockTags.REPLACEABLE_BY_TREES)
            || state.is(BlockTags.FLOWERS)
            || state.is(BlockTags.SAPLINGS)
            || state.is(BlockTags.CROPS)
            || state.is(Blocks.BAMBOO)
            || state.is(Blocks.BAMBOO_SAPLING)
            || state.is(Blocks.CACTUS)
            || state.is(Blocks.SUGAR_CANE)
            || state.is(Blocks.SWEET_BERRY_BUSH)
            || state.is(Blocks.BIG_DRIPLEAF)
            || state.is(Blocks.BIG_DRIPLEAF_STEM)
            || state.is(Blocks.SMALL_DRIPLEAF);
    }

    /**
     * Выдернуть растение целиком: сам блок и всё, что стоит на нём столбиком.
     * Бамбук и кактус тянутся вверх на добрый десяток блоков, и если убрать один
     * блок из середины, остаток повиснет в воздухе — тот же дефект, что и
     * половина дерева, только дешевле в лечении.
     */
    static void pull(ServerLevel level, BlockPos from) {
        for (BlockPos at = from; level.isLoaded(at); at = at.above()) {
            BlockState state = level.getBlockState(at);
            // Дерево останавливает подъём, хотя листва и попадает в
            // REPLACEABLE_BY_TREES: её нельзя снимать по блоку, для неё есть fell.
            if (isTree(state) || !isPlant(state)) {
                return;
            }
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    // Корни мангра — тоже древесина: без них после валки мангрового дерева в воде
    // остаётся стоять решётка корней, чего просили не оставлять.
    private static boolean isWood(BlockState state) {
        return state.is(BlockTags.LOGS)
            || state.is(Blocks.MANGROVE_ROOTS)
            || state.is(Blocks.MUDDY_MANGROVE_ROOTS)
            || state.is(Blocks.MUSHROOM_STEM);
    }

    private static boolean isCanopy(BlockState state) {
        return state.is(BlockTags.LEAVES)
            || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
            || state.is(Blocks.RED_MUSHROOM_BLOCK);
    }

    /** Что висит на дереве и после валки осталось бы в воздухе само по себе. */
    private static boolean isAttachment(BlockState state) {
        return state.is(Blocks.VINE)
            || state.is(Blocks.BEE_NEST)
            || state.is(Blocks.COCOA)
            || state.is(Blocks.MANGROVE_PROPAGULE);
    }

    /**
     * Свалить дерево, которому принадлежит {@code seed}.
     *
     * @return false только если помешал незагруженный сосед, то есть если повтор
     *         попытки может помочь. Слишком большое дерево возвращает true: оно
     *         остаётся стоять целым, и переспрашивать про него незачем.
     */
    static boolean fell(ServerLevel level, BlockPos seed) {
        Set<BlockPos> doomed = new HashSet<>();
        BlockPos wood = woodNear(level, seed);

        Result result;
        if (wood == null) {
            // Древесины рядом нет — значит это уже осиротевшая листва, и убрать её
            // целиком и есть требование «висящих листьев не остаётся».
            result = flood(level, seed, doomed, true);
        } else {
            result = flood(level, wood, doomed, false);
            if (result == Result.DONE) {
                result = canopyOf(level, doomed);
            }
        }

        if (result == Result.UNLOADED) {
            return false;
        }
        if (result == Result.TOO_BIG) {
            if (IronRoads.DEBUG) {
                IronRoads.LOGGER.debug("Дерево у {} не влезло в бюджет валки, оставлено", seed);
            }
            return true;
        }
        attachments(level, doomed);

        for (BlockPos pos : doomed) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
        return true;
    }

    /**
     * От листа в габарите — к своей древесине, спуском по убыванию
     * {@link LeavesBlock#DISTANCE}. Обходом искать нельзя: по сомкнутым кронам он
     * ушёл бы в соседнее дерево, а спуск по градиенту приводит ровно к тому
     * стволу, которому лист принадлежит.
     *
     * @return блок древесины, либо null — тогда лист ничей и висит сам по себе
     */
    private static BlockPos woodNear(ServerLevel level, BlockPos seed) {
        BlockPos at = seed;

        for (int guard = 0; guard <= LEAF_REACH; guard++) {
            if (!level.isLoaded(at)) {
                return null;
            }
            BlockState state = level.getBlockState(at);
            if (isWood(state)) {
                return at;
            }
            if (!(state.getBlock() instanceof LeavesBlock)) {
                return null;
            }
            int distance = state.getValue(LeavesBlock.DISTANCE);
            BlockPos closer = null;

            for (int dx = -1; dx <= 1 && closer == null; dx++) {
                for (int dy = -1; dy <= 1 && closer == null; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos next = at.offset(dx, dy, dz);
                        if (!level.isLoaded(next)) {
                            continue;
                        }
                        BlockState around = level.getBlockState(next);
                        if (isWood(around)) {
                            return next;
                        }
                        if (around.getBlock() instanceof LeavesBlock
                         && around.getValue(LeavesBlock.DISTANCE) < distance) {
                            closer = next;
                            break;
                        }
                    }
                }
            }
            if (closer == null) {
                return null;
            }
            at = closer;
        }
        return null;
    }

    /**
     * Разлив по связной древесине (или по связной листве, если {@code canopy}).
     * Обход по 26 соседям, а не по шести: у ванильной кроны блоки нередко
     * соприкасаются только углом.
     */
    private static Result flood(ServerLevel level, BlockPos from, Set<BlockPos> into,
                                boolean canopy) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(from);
        into.add(from);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos next = pos.offset(dx, dy, dz);
                        if (into.contains(next)) {
                            continue;
                        }
                        if (!level.isLoaded(next)) {
                            return Result.UNLOADED;
                        }
                        BlockState state = level.getBlockState(next);
                        if (canopy ? !isCanopy(state) : !isWood(state)) {
                            continue;
                        }
                        if (into.size() >= BUDGET) {
                            return Result.TOO_BIG;
                        }
                        into.add(next);
                        queue.add(next);
                    }
                }
            }
        }
        return Result.DONE;
    }

    /**
     * Добрать крону от уже собранной древесины: волнами по одному шагу, не дальше
     * {@link #LEAF_REACH}, и лист берётся, только если записанное в нём расстояние
     * до древесины не меньше числа сделанных шагов. Именно эта проверка не даёт
     * обходу перескочить в соседнее дерево там, где кроны сомкнулись.
     */
    private static Result canopyOf(ServerLevel level, Set<BlockPos> doomed) {
        Set<BlockPos> front = new HashSet<>(doomed);

        for (int step = 1; step <= LEAF_REACH && !front.isEmpty(); step++) {
            Set<BlockPos> next = new HashSet<>();

            for (BlockPos pos : front) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos at = pos.offset(dx, dy, dz);
                            if (doomed.contains(at) || next.contains(at)) {
                                continue;
                            }
                            if (!level.isLoaded(at)) {
                                return Result.UNLOADED;
                            }
                            if (ours(level.getBlockState(at), step)) {
                                next.add(at);
                            }
                        }
                    }
                }
            }
            if (doomed.size() + next.size() > BUDGET) {
                return Result.TOO_BIG;
            }
            doomed.addAll(next);
            front = next;
        }
        return Result.DONE;
    }

    private static boolean ours(BlockState state, int step) {
        if (state.getBlock() instanceof LeavesBlock) {
            return state.getValue(LeavesBlock.DISTANCE) >= step;
        }
        return isCanopy(state);
    }

    /** Лианы, гнёзда и стручки, висевшие на снесённом: иначе останутся в воздухе. */
    private static void attachments(ServerLevel level, Set<BlockPos> doomed) {
        Set<BlockPos> hanging = new HashSet<>();

        for (BlockPos pos : doomed) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos at = pos.offset(dx, dy, dz);
                        if (doomed.contains(at) || !level.isLoaded(at)) {
                            continue;
                        }
                        if (isAttachment(level.getBlockState(at))) {
                            hanging.add(at);
                        }
                    }
                }
            }
        }
        doomed.addAll(hanging);
    }
}
