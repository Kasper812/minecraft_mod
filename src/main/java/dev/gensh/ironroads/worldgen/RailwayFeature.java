package dev.gensh.ironroads.worldgen;

import com.mojang.serialization.Codec;
import dev.gensh.ironroads.IronRoads;
import dev.gensh.ironroads.geometry.BranchNetwork;
import dev.gensh.ironroads.geometry.Junctions;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.RightOfWay;
import dev.gensh.ironroads.geometry.TrackProfiler;
import dev.gensh.ironroads.geometry.TunnelProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;

public class RailwayFeature extends Feature<NoneFeatureConfiguration> {
    public static final int SPACING = 1536;

    public static final int ALIGNMENT = 40;

    public static final int MAX_GRADE_RUN = 12;

    public static final int BED_HALF_WIDTH = 4;

    public static final int BRANCH_HALF_WIDTH = 2;

    public static final int TRACK_OFFSET = TrackBuilder.TRACK_OFFSET;

    private static final int[] MAIN_TRACKS = { -TRACK_OFFSET, +TRACK_OFFSET };
    private static final int[] BRANCH_TRACKS = { 0 };

    public static final int STATION_OFFSET = BRANCH_HALF_WIDTH;

    public static final int GAUGE_HEIGHT = 4;

    /** Полос откоса за кромкой полотна: склон 1:1 под насыпь высотой 5. */
    public static final int BANK_WINGS = 4;

    /**
     * Радиус поиска линий у чанка. Считается от самой дальней колонки, которую
     * трогает фича, а не от полотна: подошва откоса лежит на BANK_WINGS блоков
     * дальше кромки, и при узком радиусе она пропадала бы на дальнем краю чанка.
     */
    public static final int REACH = 8 + BED_HALF_WIDTH + BANK_WINGS;

    /** Полос откоса выемки. */
    private static final int CUT_WINGS = 3;

    /**
     * Предел полосы отвода. Ровно {@code max(CUT_WINGS, BANK_WINGS)}, и это не
     * совпадение: полоса не имеет права уходить дальше самой дальней колонки,
     * которую фича трогает и так, иначе придётся пересчитывать бюджет региона в
     * {@link #touchesChunk}. Ширину полосы в конкретной точке выбирает
     * {@link RightOfWay}.
     */
    private static final int ROW_WINGS = Math.max(CUT_WINGS, BANK_WINGS);

    /**
     * Насколько далеко по высоте от полотна земля ещё может вырастить дерево,
     * достающее до габарита. Полоса отвода занимает только такую землю: колонка на
     * тридцать блоков ниже эстакады или на тридцать выше дна выемки безопасна сама
     * по себе, и щебень там — просто шрам поперёк нетронутого склона.
     *
     * ponytail: гигантская тропическая сосна, вставшая ровно под высоким виадуком,
     * может достать кроной до настила снизу. Это под мостом, из окна не видно;
     * поднимать, если окажется заметно.
     */
    private static final int TREE_REACH = 16;

    /** На сколько блоков высоты стенка выемки отступает за полосу. */
    private static final int CUT_BATTER = 2;

    /**
     * Глубже этого выемку не роем — там тоннель. Порог низкий намеренно: открытая
     * траншея в двадцать блоков глубиной с отвесными стенками и была тем «уродливым
     * провалом над путём», ради которого переписан весь этот кусок.
     */
    private static final int MAX_CUTTING = 12;

    private static final int JUNCTION_CUTTING = 20;

    /** Глубина выемки, с которой борт одевают подпорной стенкой. */
    private static final int REVETMENT = 5;

    private static final int REVETMENT_HEIGHT = 3;

    /**
     * Запас сечений по краям чанка. Ровно столько нужно, чтобы TunnelProfile.settle
     * (радиус SETTLE_RADIUS, четыре прохода) и поиск устья (PORTAL_DEPTH) дали для
     * любого сечения внутри чанка тот же ответ, что и у соседнего чанка. Меньше —
     * и на границе чанков тоннель начнёт рваться.
     */
    private static final int COVER_MARGIN = 10;

    private static final int SETTLE_RADIUS = 2;

    private static final int PORTAL_DEPTH = 2;

    private static final int JUNCTION_CLEAR = Junctions.REACH + 6;

    /** Сколько блоков сыпучего разбирается над расчисткой. */
    private static final int FALL_PROBE = 8;

    private static final int SLOW_CHUNK_MILLIS = 50;

    public RailwayFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;

        TrackProfiler profiler = RailwayProfilers.forLevel(level.getLevel(),
                                                          context.chunkGenerator());
        RailNetwork network = profiler.network();

        boolean placedAnything = false;
        int spurReach = REACH + Junctions.REACH;
        long startedAt = System.nanoTime();

        List<Line> mainLines = network.linesNear(chunkMinX + 8, chunkMinZ + 8, REACH);
        BranchNetwork spurs = new BranchNetwork(network);
        List<BranchNetwork.Branch> branches =
                spurs.branchesNear(chunkMinX + 8, chunkMinZ + 8, spurReach);

        List<BranchNetwork.Halt> halts =
                spurs.haltsNear(chunkMinX + 8, chunkMinZ + 8, REACH);

        TrackBuilder trackBuilder = new TrackBuilder(network, profiler, branches);

        placedAnything |= buildJunctions(level, profiler, chunkMinX, chunkMinZ);

        for (Line line : mainLines) {
            placedAnything |= build(level, profiler, trackBuilder, line,
                                    chunkMinX, chunkMinZ, BED_HALF_WIDTH, MAIN_TRACKS,
                                    Integer.MIN_VALUE, Integer.MAX_VALUE, null, branches, halts);
        }
        for (BranchNetwork.Branch branch : branches) {
            placedAnything |= build(level, profiler, trackBuilder, branch.line(),
                                    chunkMinX, chunkMinZ, BRANCH_HALF_WIDTH, BRANCH_TRACKS,
                                    Math.min(branch.fromAlong(), branch.toAlong()),
                                    Math.max(branch.fromAlong(), branch.toAlong()),
                                    branch, branches, halts);
        }

        long millis = (System.nanoTime() - startedAt) / 1_000_000;
        if (millis >= SLOW_CHUNK_MILLIS && IronRoads.DEBUG) {
            IronRoads.LOGGER.debug("Чанк {},{} строился {} мс",
                                   chunkMinX >> 4, chunkMinZ >> 4, millis);
        }
        return placedAnything;
    }

    private boolean build(WorldGenLevel level, TrackProfiler profiler, TrackBuilder trackBuilder,
                          Line line, int chunkMinX, int chunkMinZ, int halfWidth,
                          int[] trackOffsets, int alongMin, int alongMax,
                          BranchNetwork.Branch branch,
                          List<BranchNetwork.Branch> branches,
                          List<BranchNetwork.Halt> halts) {
        boolean placed = false;
        RailNetwork network = profiler.network();
        boolean alongZ = line.axis() == Line.Axis.Z;
        int alongStart = alongZ ? chunkMinZ : chunkMinX;
        int widest = halfWidth + Math.max(CUT_WINGS, BANK_WINGS);

        if (alongStart + 15 < alongMin || alongStart > alongMax
         || !touchesChunk(profiler, line, alongStart, alongZ ? chunkMinX : chunkMinZ, widest)) {
            return false;
        }
        boolean[] tunnels = tunnelSections(level, profiler, line, alongStart, halfWidth);

        for (int step = 0; step < 16; step++) {
            int along = alongStart + step;
            if (along < alongMin || along > alongMax) {
                continue;
            }
            int perpendicular = profiler.perpendicularAt(line, along);
            int y = profiler.heightAt(line, along);
            boolean gap = profiler.isGap(line, along);

            int section = step + COVER_MARGIN;
            boolean tunnel = tunnels[section];
            boolean mouth = tunnel && (!tunnels[section - 1] || !tunnels[section + 1]);
            boolean portal = tunnel && (mouth
                                    || !tunnels[section - PORTAL_DEPTH]
                                    || !tunnels[section + PORTAL_DEPTH]);
            // Предпортальная выемка раскрывается на всю ширину щёковых стенок:
            // иначе портал стоит внутри горы и снаружи видно только дыру в скале.
            boolean approach = !tunnel && (tunnels[section - 1] || tunnels[section + 1]
                                        || tunnels[section - PORTAL_DEPTH]
                                        || tunnels[section + PORTAL_DEPTH]);
            boolean railing = line.isBranch()
                           || clearOfJunctions(network, line, along, branches);

            int outer = tunnel ? TunnelBuilder.reach(halfWidth, portal) : widest;

            // Полоса отвода считается на сечение, а не на колонку: обе стороны
            // независимы, и обе гуляют по длине — просека выходит извилистой.
            int rowLeft = RightOfWay.wings(line, along, -1, ROW_WINGS);
            int rowRight = RightOfWay.wings(line, along, +1, ROW_WINGS);

            for (int offset = -outer; offset <= outer; offset++) {
                int x = alongZ ? perpendicular + offset : along;
                int z = alongZ ? along : perpendicular + offset;

                if (x < chunkMinX || x >= chunkMinX + 16
                 || z < chunkMinZ || z >= chunkMinZ + 16) {
                    continue;
                }
                if (Junctions.insideFootprint(network.spacing(), x, z)) {
                    placed = true;
                    continue;
                }

                if (tunnel) {
                    boreColumn(level, x, y, z, offset, along, halfWidth, portal, mouth, gap);
                    placed = true;
                    continue;
                }

                int ground = rockTop(level, x, z);
                int wing = Math.abs(offset) - halfWidth;

                if (wing > 0) {
                    if (approach && wing <= PORTAL_DEPTH) {
                        clearToSky(level, x, y, z, y);
                    } else if (wing <= CUT_WINGS) {
                        cutBack(level, x, y, z, wing);
                    }
                    // Откос отсыпается и под пандусом тоже. Пока он там не
                    // отсыпался, насыпь на подъёме обрывалась отвесной стенкой и
                    // снова начиналась за разрывом — самый заметный признак того,
                    // что дорогу вставили в мир, а не построили.
                    SupportBuilder.skirt(level, x, y, z, ground, wing);
                    reserve(level, x, y, z, wing, offset < 0 ? rowLeft : rowRight);
                    continue;
                }

                boolean edge = wing == 0;
                boolean revet = edge && !gap && ground - y >= REVETMENT;

                // Под кривой полотна нет, поэтому расчистка спускается на два блока
                // ниже головки рельса; вверх она уходит так же, как на прямой — до
                // дневной поверхности. Коробка с потолком в GAUGE_HEIGHT + 2
                // оставляла над пандусом в выемке козырёк грунта.
                clearToSky(level, x, gap ? y - 2 : y, z, y);
                SupportBuilder.support(level, x, y, z, ground, edge, along,
                                       railing && !gap);
                if (revet) {
                    revetment(level, x, y, z);
                }

                if (!gap) {
                    BlockState bed = bedBlock(offset, x, y, z, halfWidth,
                                              y - ground, revet);
                    if (bed != null) {
                        level.setBlock(new BlockPos(x, y - 1, z), bed, 2);
                    }
                }
                // Полотно тоже занимается — последним, когда всё уложено. Потёртая
                // кромка и колонка под кривой не получают покрытия вовсе, и без
                // этой строчки под саженец там оставалась трава прямо у рельса.
                reserve(level, x, y, z, wing, offset < 0 ? rowLeft : rowRight);
                placed = true;
            }

            if (gap) {
                continue;
            }

            for (int lateral : trackOffsets) {
                trackBuilder.layTrack(level, line, along, lateral, chunkMinX, chunkMinZ);
            }
            if (branch == null) {
                FurnitureBuilder.halt(level, profiler, halts, line, along,
                                      chunkMinX, chunkMinZ);
            } else {
                FurnitureBuilder.station(level, profiler, branch, along,
                                         chunkMinX, chunkMinZ);
                FurnitureBuilder.platformLight(level, profiler, branch, along, y,
                                               chunkMinX, chunkMinZ);
            }
        }
        return placed;
    }

    private void boreColumn(WorldGenLevel level, int x, int y, int z, int offset, int along,
                            int halfWidth, boolean portal, boolean mouth, boolean gap) {
        TunnelBuilder.section(level, x, y - (gap ? 2 : 1), y, z, offset, along,
                              halfWidth, portal, mouth);

        // Сечение может выйти из склона наружу — так получается галерея, и ей нужна
        // забутовка до земли, иначе кладка и полотно висят в воздухе над обрывом.
        // Начинается она сразу под подошвой сечения: иначе между ней и стеной
        // остаётся щель в блок. Под настоящей горой ground выше рельса, и pave
        // ничего не делает — цена такой проверки нулевая.
        SupportBuilder.pave(level, x, y - (gap ? 1 : 0), z, rockTop(level, x, z));

        if (gap) {
            return;
        }
        // Пол настилается на всю выработку, а не только под полотном: выработка
        // шире полотна, и по краям оставалась бы канава вдоль обеих стен.
        int bore = TunnelProfile.bore(halfWidth);
        if (Math.abs(offset) <= bore) {
            // В тоннеле кромку полотна не рвём: дыра в полу под стеной читается
            // как недостроенное, а не как истёртое.
            level.setBlock(new BlockPos(x, y - 1, z),
                           Palette.pick(bedKind(offset, bore), x, y - 1, z), 2);
        }
        if (offset == 0 && TunnelBuilder.wantsShaft(along)) {
            TunnelBuilder.shaft(level, x, y, z, halfWidth);
        }
    }

    /**
     * Какие сечения линии внутри чанка идут тоннелем.
     *
     * Решение принимается по настоящей карте высот чанка, а не по сглаженному
     * профилю: сглаживание на радиусе 64 занижает гору вдвое, и в неё врубались
     * открытой траншеей вместо тоннеля — это и был главный дефект прошлой версии.
     *
     * Два повода уйти под землю. Первый: коридор укрыт породой целиком — обычный
     * тоннель, и портал сам собой встаёт там, где кончается укрытие. Второй:
     * выемка вышла бы глубже MAX_CUTTING — рыть такую траншею дороже, чем
     * пробить свод, и настоящая дорога так и делает. Второй повод накладывается
     * после склейки: иначе склейка выбросила бы короткий участок сквозь скальный
     * гребень и там осталась бы яма в тридцать блоков.
     */
    private boolean[] tunnelSections(WorldGenLevel level, TrackProfiler profiler,
                                     Line line, int alongStart, int halfWidth) {
        int span = 16 + 2 * COVER_MARGIN;
        boolean[] raw = new boolean[span];
        boolean[] deep = new boolean[span];

        boolean alongZ = line.axis() == Line.Axis.Z;
        int buried = TunnelProfile.shellTop(TunnelProfile.bore(halfWidth))
                   + TunnelBuilder.MIN_COVER;

        for (int i = 0; i < span; i++) {
            int along = alongStart + i - COVER_MARGIN;
            int y = profiler.heightAt(line, along);
            int perpendicular = profiler.perpendicularAt(line, along);

            int least = Integer.MAX_VALUE;
            int most = Integer.MIN_VALUE;
            for (int offset = -halfWidth; offset <= halfWidth; offset++) {
                int x = alongZ ? perpendicular + offset : along;
                int z = alongZ ? along : perpendicular + offset;
                int cover = rockTop(level, x, z) - y;
                least = Math.min(least, cover);
                most = Math.max(most, cover);
            }
            deep[i] = most > MAX_CUTTING;
            raw[i] = least >= buried || deep[i];
        }

        boolean[] settled = TunnelProfile.settle(raw, SETTLE_RADIUS);
        for (int i = 0; i < span; i++) {
            settled[i] |= deep[i];
        }
        return settled;
    }

    /**
     * Проходит ли линия через чанк. {@code linesNear} отбирает линии по базовой
     * оси с запасом на весь MAX_SHIFT, поэтому в списке регулярно оказываются
     * линии, которые после сдвига идут мимо чанка. Строить по ним нечего, а читать
     * под них рельеф прямо опасно: выборка укрытия уходит на COVER_MARGIN блоков за
     * край чанка, и у пролетевшей мимо линии она вышла бы за регион фичи — то есть
     * потянула бы генерацию соседнего чанка прямо из place().
     *
     * Бюджет региона: 24 блока от центра чанка. Дальняя выборка — это
     * {@code 8 + widest} (условие ниже) плюс два блока увода трассы на
     * COVER_MARGIN блоков вдоль (сдвиг не быстрее 8 блоков на 48) плюс halfWidth
     * вбок = 8 + 8 + 2 + 4 = 22. Расширять полосы — только пересчитав эту сумму.
     */
    private boolean touchesChunk(TrackProfiler profiler, Line line, int alongStart,
                                 int chunkMin, int widest) {
        for (int step = 0; step < 16; step++) {
            int perpendicular = profiler.perpendicularAt(line, alongStart + step);
            if (perpendicular + widest >= chunkMin
             && perpendicular - widest < chunkMin + 16) {
                return true;
            }
        }
        return false;
    }

    private boolean buildJunctions(WorldGenLevel level, TrackProfiler profiler,
                                   int chunkMinX, int chunkMinZ) {
        RailNetwork network = profiler.network();
        int spacing = network.spacing();
        boolean placed = false;

        for (int x = chunkMinX; x < chunkMinX + 16; x++) {
            for (int z = chunkMinZ; z < chunkMinZ + 16; z++) {
                if (!Junctions.insideFootprint(spacing, x, z)) {
                    continue;
                }
                int nodeX = Junctions.nodeCoordinate(spacing, x);
                int nodeZ = Junctions.nodeCoordinate(spacing, z);
                int y = profiler.heightAt(new Line(Line.Axis.Z,
                                                   Math.floorDiv(nodeX, spacing)), nodeZ);

                // У развязки своя, более щедрая глубина срезки: тоннеля на узле не
                // бывает (все четыре съезда обязаны выйти на дневную поверхность),
                // и порог перегона оставил бы над развязкой козырёк.
                clearTo(level, x, y, z, JUNCTION_CUTTING);
                SupportBuilder.pave(level, x, y, z, rockTop(level, x, z));
                level.setBlock(new BlockPos(x, y - 1, z),
                               Palette.pick(junctionKind(spacing, x, z), x, y - 1, z), 2);
                placed = true;
            }
        }
        return placed;
    }

    private Palette.Kind junctionKind(int spacing, int x, int z) {
        int dx = Math.abs(Junctions.offsetFromNode(spacing, x));
        int dz = Math.abs(Junctions.offsetFromNode(spacing, z));
        return dx <= TRACK_OFFSET || dz <= TRACK_OFFSET
             ? Palette.Kind.BALLAST
             : Palette.Kind.SHOULDER;
    }

    private boolean clearOfJunctions(RailNetwork network, Line line, int along,
                                     List<BranchNetwork.Branch> branches) {
        int spacing = network.spacing();
        int distance = Math.floorMod(along, spacing);
        if (Math.min(distance, spacing - distance) <= JUNCTION_CLEAR) {
            return false;
        }
        for (BranchNetwork.Branch spur : branches) {
            if (spur.line().axis() == line.axis()
             || spur.line().anchorAlong() != line.index() * spacing) {
                continue;
            }
            int junction = spur.line().index() * spacing + spur.line().offset();
            if (Math.abs(along - junction) <= JUNCTION_CLEAR) {
                return false;
            }
        }
        return true;
    }

    private Palette.Kind bedKind(int offset, int halfWidth) {
        if (Math.abs(offset) == halfWidth) {
            return Palette.Kind.VERGE;
        }
        return Math.abs(offset) <= TrackBuilder.TRACK_OFFSET
             ? Palette.Kind.BALLAST
             : Palette.Kind.SHOULDER;
    }

    // Крайняя колонка — обочина, и местами её просто нет: ровная кромка по линейке
    // на весь перегон читается как чертёж. Сплошной кромка остаётся под перилами
    // эстакады и под подпорной стенкой выемки: и то и другое, повиснув над дырой,
    // читается как недостроенное, а не как истёртое.
    private BlockState bedBlock(int offset, int x, int y, int z, int halfWidth,
                                int rise, boolean revet) {
        Palette.Kind kind = bedKind(offset, halfWidth);
        if (kind == Palette.Kind.VERGE && !revet
         && rise < SupportBuilder.RAILING_HEIGHT && Palette.frayed(x, z)) {
            return null;
        }
        return Palette.pick(kind, x, y - 1, z);
    }

    /**
     * Откос выемки: стенка отступает наружу на CUT_BATTER блока высоты за полосу.
     * Отвесная стенка — это прорубленная в скале щель, а не выемка; настоящая
     * выемка всегда раскрыта кверху.
     */
    private void cutBack(WorldGenLevel level, int x, int y, int z, int wing) {
        int foot = y + wing * CUT_BATTER;
        int top = Math.min(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z),
                           y + MAX_CUTTING + wing * CUT_BATTER);
        if (top > foot) {
            clear(level, x, foot + 1, z, top);
        }
    }

    /**
     * Занять колонку под дорогу.
     *
     * Правило одно и без исключений: если верх колонки — то, на чём укореняется
     * растительность, он заменяется материалом полосы. Всё остальное — камень,
     * песок, собственная кладка — остаётся как было, поэтому на скальном склоне
     * полоса вообще ничего не делает и каменной ленты поперёк горы не появляется.
     *
     * Проверка идёт по {@link BlockTags#DIRT}, потому что по нему же ванильная
     * растительность и решает, где ей расти: {@code BushBlock.mayPlaceOn} — это
     * дословно «блок под собой в теге DIRT или грядка». Не список блоков, не
     * порог по высоте — тот же предикат, что у самой игры. Оттого правило
     * работает и с модовыми биомами, и с модовыми деревьями: они спрашивают то
     * же самое.
     *
     * До этого полоса клалась, только если верх колонки лежал в двух блоках от
     * полотна, а кромка полотна вдобавок местами не клалась вовсе
     * ({@link #bedBlock} возвращает null для потёртой кромки). На любом уклоне и
     * на каждой потёртой колонке под саженец оставалась трава — вплотную к пути.
     * Ровно оттуда деревья и лезли на рельсы.
     *
     * Крайняя колонка полосы пропускается через раз ({@link Palette#frayed}) —
     * это не дыра в правиле, а граница: опушка входит в просеку языками и выходит
     * из неё, вместо того чтобы обрываться по линейке. Первая колонка за кромкой
     * не пропускается никогда, поэтому сплошная полоса шириной хотя бы в блок
     * есть всегда.
     */
    private void reserve(WorldGenLevel level, int x, int y, int z, int wing, int wings) {
        if (wing > wings || (wing == wings && wings > 1 && Palette.frayed(x, z))) {
            return;
        }
        int top = rockTop(level, x, z);
        if (Math.abs(top - (y - 1)) > TREE_REACH) {
            return;
        }
        BlockPos pos = new BlockPos(x, top, z);

        if (!level.getBlockState(pos).is(BlockTags.DIRT)) {
            return;
        }
        level.setBlock(pos, Palette.pick(Palette.Kind.VERGE, x, top, z), 2);
    }

    /**
     * Подпорная стенка на борту глубокой выемки: у настоящей дороги борт одет
     * камнем, а не оставлен сырым грунтом, который через год осыплется на путь.
     */
    private void revetment(WorldGenLevel level, int x, int y, int z) {
        for (int cy = y; cy < y + REVETMENT_HEIGHT; cy++) {
            level.setBlock(new BlockPos(x, cy, z),
                           Palette.pick(Palette.Kind.LINING, x, cy, z), 2);
        }
    }

    /**
     * Расчистка над полотном — до настоящей дневной поверхности, без потолка.
     *
     * Потолок в MAX_CUTTING тут стоял зря и вредил: глубже MAX_CUTTING открытых
     * сечений не бывает (иначе {@code tunnelSections} отдало бы тоннель), так что
     * ограничивать нечего, а на границе решения он оставлял над путём козырёк
     * грунта, который снизу читается как висящая в воздухе земля.
     *
     * @param from откуда чистим: под кривой на два блока ниже головки рельса
     * @param railY головка рельса — от неё считается габарит
     */
    private void clearToSky(WorldGenLevel level, int x, int from, int z, int railY) {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        clear(level, x, from, z, Math.max(railY + GAUGE_HEIGHT, surface));
    }

    private void clearTo(WorldGenLevel level, int x, int y, int z, int cap) {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        clear(level, x, y, z, Math.min(Math.max(y + GAUGE_HEIGHT, surface), y + cap));
    }

    private void clear(WorldGenLevel level, int x, int from, int z, int top) {
        for (int cy = from; cy <= top; cy++) {
            BlockPos pos = new BlockPos(x, cy, z);
            if (level.getBlockState(pos).isAir()) {
                continue;
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }

        // Столб сыпучего над расчисткой разбираем весь, а не один блок: setBlock с
        // флагом 2 не рассылает обновлений соседям, поэтому песок над дырой не
        // осыплется сам, а останется висеть — это и есть «земля в воздухе».
        for (int cy = top + 1; cy <= top + FALL_PROBE; cy++) {
            BlockPos above = new BlockPos(x, cy, z);
            BlockState resting = level.getBlockState(above);
            if (resting.isAir()) {
                return;
            }
            if (!(resting.getBlock() instanceof FallingBlock)
             && !resting.canBeReplaced()
             && level.getFluidState(above).isEmpty()) {
                return;
            }
            level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    /** Верх породы: без воды, без растительности, и потому стабилен. */
    private int rockTop(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
    }
}
