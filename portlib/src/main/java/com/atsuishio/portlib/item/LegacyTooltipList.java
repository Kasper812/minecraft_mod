package com.atsuishio.portlib.item;

import net.minecraft.network.chat.Component;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LegacyTooltipList extends AbstractList<Component> {
    private final List<Component> lines = new ArrayList<>();
    private final Consumer<Component> consumer;

    public LegacyTooltipList(Consumer<Component> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Component get(int index) {
        return lines.get(index);
    }

    @Override
    public int size() {
        return lines.size();
    }

    @Override
    public boolean add(Component component) {
        lines.add(component);
        consumer.accept(component);
        return true;
    }

    @Override
    public void add(int index, Component component) {
        lines.add(index, component);
        consumer.accept(component);
    }

    @Override
    public Component set(int index, Component component) {
        return lines.set(index, component);
    }

    @Override
    public Component remove(int index) {
        return lines.remove(index);
    }
}
