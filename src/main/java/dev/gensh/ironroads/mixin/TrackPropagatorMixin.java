package dev.gensh.ironroads.mixin;

import com.simibubi.create.content.trains.track.TrackPropagator;
import dev.gensh.ironroads.graph.LoadedOnly;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Единственная точка, где обход графа путей Create получает мир.
 *
 * <p>Почему миксин, а не вызов из своего кода. Обход запускается тремя путями,
 * и свой у нас только один: {@code SlopeLinker.registerTracks} зовёт
 * {@code onRailAdded} сам, {@code TrackBlock.onPlace} планирует тик на любую
 * постановку пути, а {@code TrackBlockEntity.addConnection} планирует такой же
 * тик на каждую натянутую кривую — и оба тика приходят в
 * {@code TrackBlock.tick}, который зовёт {@code onRailAdded} с настоящим
 * {@code ServerLevel}. Заткнуть свой вызов и не заткнуть тик — значит не
 * починить ничего: в дампах watchdog'а стоит именно {@code TrackBlock.tick}.
 * Подмена аргумента здесь накрывает все три пути разом.
 *
 * <p>Что подменяется — см. {@link LoadedOnly}: мир, отвечающий «воздух» вместо
 * того, чтобы синхронно сгенерировать чанк. Сам граф Create не трогается.
 *
 * <p>{@code remap = false} обязателен и не случаен: {@code TrackPropagator} и
 * {@code onRailAdded} — имена Create, они переживают reobf как есть, поэтому
 * миксин работает и в dev, и в собранном jar'е без refmap'а.
 */
@Mixin(value = TrackPropagator.class, remap = false)
public class TrackPropagatorMixin {
    @ModifyVariable(method = "onRailAdded", at = @At("HEAD"), argsOnly = true, index = 0)
    private static LevelAccessor ironroads$stopAtUnloadedOnAdd(LevelAccessor level) {
        return LoadedOnly.wrap(level);
    }

    // Снятие рельса ходит тем же обходом и вешает поток тем же способом: рельс,
    // сломанный у края прогрузки, тянет генерацию так же, как поставленный.
    // Один и тот же дефект, одно и то же лечение.
    @ModifyVariable(method = "onRailRemoved", at = @At("HEAD"), argsOnly = true, index = 0)
    private static LevelAccessor ironroads$stopAtUnloadedOnRemove(LevelAccessor level) {
        return LoadedOnly.wrap(level);
    }
}
