package cn.sh1rocu.simplebedrockmodel.client.port;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

public class SubmitBufferSource implements MultiBufferSource, AutoCloseable {
    private static final PoseStack IDENTITY = new PoseStack();
    private static final ThreadLocal<Deque<SubmitBufferSource>> ACTIVE = ThreadLocal.withInitial(ArrayDeque::new);

    private final SubmitNodeCollector collector;
    private final Map<RenderType, RecordedGeometry> pending = new LinkedHashMap<>();
    private boolean closed;

    public SubmitBufferSource(SubmitNodeCollector collector) {
        this.collector = collector;
        ACTIVE.get().push(this);
    }

    public static @Nullable SubmitBufferSource current() {
        return ACTIVE.get().peek();
    }

    public SubmitNodeCollector collector() {
        return collector;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return pending.computeIfAbsent(renderType, t -> new RecordedGeometry());
    }

    public void submitPending() {
        for (Map.Entry<RenderType, RecordedGeometry> entry : pending.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                collector.submitCustomGeometry(IDENTITY, entry.getKey(), entry.getValue());
            }
        }
        pending.clear();
    }

    public void flush() {
        submitPending();
        if (!closed) {
            closed = true;
            ACTIVE.get().remove(this);
        }
    }

    @Override
    public void close() {
        flush();
    }
}
