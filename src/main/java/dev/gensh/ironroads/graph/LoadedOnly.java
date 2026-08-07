package dev.gensh.ironroads.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * Взгляд на мир, обрывающийся на границе загруженного.
 *
 * <p>Зачем. {@code TrackPropagator.onRailAdded} обходит связный путь через
 * {@code Level.getBlockState}, а тот на незагруженном чанке уходит в
 * {@code ServerChunkCache.getChunk} и <b>синхронно генерирует чанк прямо в
 * серверном потоке</b>. Магистраль этого мода — сплошная нить на километры, и
 * обход (бюджет Create — 1000 узлов) уезжает с неё чанков на шестьдесят: каждый
 * из них генерируется, в каждом снова кладётся путь, каждый снова будит обход.
 * Это и есть замеченные тики в 122 секунды, и виноват не мод, а то, что графу
 * Create отдали мир, умеющий блокировать поток ради генерации.
 *
 * <p>Что делаем. Отдаём графу мир, который на незагруженном чанке отвечает
 * «воздух» вместо того, чтобы его сгенерировать. Обход упирается в пустоту и
 * заканчивается — ровно то, чего просит правило «дошли до незагруженного —
 * вернулись и отложили». Ни одного чанка при этом не загружается, ни один
 * {@code getChunk} не зовётся. Оставшиеся рельсы попадут в граф, когда их чанк
 * загрузится сам: {@link RailwayGraphLinker} сшивает каждый загруженный чанк, и
 * граф Create прирастает кусками по мере появления мира. Логика самого графа не
 * трогается — подменена только карта, по которой он ходит.
 *
 * <p>Почему прокси, а не класс с полусотней делегирующих методов: {@code
 * LevelAccessor} — интерфейс на несколько десятков методов, из которых нам
 * нужны три. Прокси зовётся считанные разы на чанк (обход берёт один рельс на
 * перегон, см. {@code SlopeLinker.registerTracks}), так что рефлексия здесь
 * дешевле пары сотен строк, которые пришлось бы читать в три часа ночи.
 *
 * <p>Почему обёртка не {@code ServerLevel}: {@code ITrackBlock.walkConnectedTracks}
 * при {@code instanceof ServerLevel} лезет за настоящим миром через
 * {@code getServer().getLevel(...)} и обёртку выбрасывает. Обёртка обязана быть
 * «просто» {@code LevelAccessor}, иначе подмена не имеет силы.
 */
public final class LoadedOnly {
    /**
     * Обёртка ставится только в оверворлде — и это не осторожность, а
     * требование корректности. {@code ITrackBlock.getConnected} берёт измерение
     * узла как {@code world instanceof Level ? level.dimension() : OVERWORLD}.
     * Обёртка не {@code Level} (см. выше), значит измерение узла всегда выйдет
     * оверворлдом. Там, где это правда, подмена незаметна; в Нижнем мире она
     * испортила бы ключи узлов чужому, обычному пути Create. Дороги мода живут
     * в оверворлде (biome_modifier на {@code #minecraft:is_overworld}), поэтому
     * зависание лечится там, где оно есть, и нигде больше.
     */
    public static LevelAccessor wrap(LevelAccessor level) {
        if (!(level instanceof ServerLevel server)
         || !server.dimension().equals(Level.OVERWORLD)) {
            return level;
        }
        return (LevelAccessor) Proxy.newProxyInstance(
                LoadedOnly.class.getClassLoader(),
                new Class<?>[] { LevelAccessor.class },
                (proxy, method, args) -> {
                    Object shortCircuit = outsideLoaded(server, method.getReturnType(), args);
                    if (shortCircuit != PASS) {
                        return shortCircuit;
                    }
                    try {
                        return method.invoke(server, args);
                    } catch (InvocationTargetException wrapped) {
                        throw wrapped.getCause();
                    }
                });
    }

    private static final Object PASS = new Object();

    /**
     * Ответ за незагруженный чанк — или {@link #PASS}, если вопрос не про блок.
     *
     * <p>Метод узнаётся по типу возврата, а не по имени: в собранном jar'е имена
     * ванильных методов переписаны в SRG ({@code getBlockState} становится
     * {@code m_8055_}), и сравнение со строкой работало бы только в dev. Имена
     * классов не переписываются никогда, поэтому по ним сравнивать можно.
     */
    private static Object outsideLoaded(ServerLevel level, Class<?> returns, Object[] args) {
        if (args == null || args.length != 1 || !(args[0] instanceof BlockPos pos)) {
            return PASS;
        }
        if (level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return PASS;
        }
        if (returns == BlockState.class) {
            return Blocks.AIR.defaultBlockState();
        }
        if (returns == FluidState.class) {
            return Fluids.EMPTY.defaultFluidState();
        }
        if (BlockEntity.class.isAssignableFrom(returns)) {
            return null;
        }
        return PASS;
    }

    private LoadedOnly() {}
}
