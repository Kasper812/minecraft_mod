package com.atsuishio.portlib.render.clip;

import com.atsuishio.portlib.render.MultiBufferSource;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ClippingBufferSource implements MultiBufferSource.BufferSource {
    private final MultiBufferSource.BufferSource delegate;
    private final List<ScreenClipRegion> insideRegions;
    private final List<ScreenClipRegion> outsideRegions;
    private final Map<RenderType, ClippingVertexConsumer> consumers = new IdentityHashMap<>();

    public ClippingBufferSource(MultiBufferSource.BufferSource delegate, List<ScreenClipRegion> insideRegions, List<ScreenClipRegion> outsideRegions) {
        this.delegate = delegate;
        this.insideRegions = new ArrayList<>(insideRegions);
        this.outsideRegions = new ArrayList<>(outsideRegions);
    }

    public static MultiBufferSource.BufferSource of(MultiBufferSource.BufferSource delegate, List<ScreenClipRegion> insideRegions, List<ScreenClipRegion> outsideRegions) {
        List<ScreenClipRegion> inside = new ArrayList<>();
        List<ScreenClipRegion> outside = new ArrayList<>();
        for (ScreenClipRegion region : insideRegions) {
            if (region != null) {
                inside.add(region);
            }
        }
        for (ScreenClipRegion region : outsideRegions) {
            if (region != null) {
                outside.add(region);
            }
        }
        if (inside.isEmpty() && outside.isEmpty()) {
            return delegate;
        }
        return new ClippingBufferSource(delegate, inside, outside);
    }

    public static MultiBufferSource.BufferSource inside(MultiBufferSource.BufferSource delegate, ScreenClipRegion region) {
        return of(delegate, java.util.Collections.singletonList(region), List.of());
    }

    public static MultiBufferSource.BufferSource outside(MultiBufferSource.BufferSource delegate, List<ScreenClipRegion> regions) {
        return of(delegate, List.of(), regions);
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        ClippingVertexConsumer consumer = this.consumers.get(renderType);
        VertexConsumer target = this.delegate.getBuffer(renderType);
        if (consumer == null || consumer.target() != target) {
            if (consumer != null) {
                consumer.finish();
            }
            PrimitiveTopology topology = renderType.pipeline().getPrimitiveTopology();
            int length = topology == PrimitiveTopology.QUADS ? 4 : topology == PrimitiveTopology.TRIANGLES ? 3 : 0;
            if (length == 0) {
                return target;
            }
            consumer = new ClippingVertexConsumer(target, length, this.insideRegions, this.outsideRegions);
            this.consumers.put(renderType, consumer);
        }
        return consumer;
    }

    public void finish() {
        for (ClippingVertexConsumer consumer : this.consumers.values()) {
            consumer.finish();
        }
    }

    @Override
    public void endBatch() {
        finish();
        this.delegate.endBatch();
    }

    @Override
    public void endBatch(RenderType renderType) {
        ClippingVertexConsumer consumer = this.consumers.get(renderType);
        if (consumer != null) {
            consumer.finish();
        }
        this.delegate.endBatch(renderType);
    }
}
