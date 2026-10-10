package com.atsuishio.portlib.forge.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EntityMountEvent {
    public static final List<Consumer<EntityMountEvent>> LISTENERS = new CopyOnWriteArrayList<>();

    private final Entity entityMounting;
    private final Entity entityBeingMounted;
    private final Level level;
    private final boolean mounting;
    private boolean canceled;

    public EntityMountEvent(Entity entityMounting, Entity entityBeingMounted, Level level, boolean mounting) {
        this.entityMounting = entityMounting;
        this.entityBeingMounted = entityBeingMounted;
        this.level = level;
        this.mounting = mounting;
    }

    public Entity getEntityMounting() {
        return this.entityMounting;
    }

    public Entity getEntityBeingMounted() {
        return this.entityBeingMounted;
    }

    public Level getLevel() {
        return this.level;
    }

    public boolean isMounting() {
        return this.mounting;
    }

    public boolean isDismounting() {
        return !this.mounting;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public static boolean post(Entity mounting, Entity mounted, boolean isMounting) {
        if (LISTENERS.isEmpty()) {
            return false;
        }
        EntityMountEvent event = new EntityMountEvent(mounting, mounted, mounting.level(), isMounting);
        for (Consumer<EntityMountEvent> l : LISTENERS) {
            l.accept(event);
        }
        return event.isCanceled();
    }
}
