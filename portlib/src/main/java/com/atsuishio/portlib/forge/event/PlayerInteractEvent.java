package com.atsuishio.portlib.forge.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class PlayerInteractEvent {
    public static final List<Consumer<EntityInteract>> ENTITY_INTERACT = new CopyOnWriteArrayList<>();
    public static final List<Consumer<EntityInteractSpecific>> ENTITY_INTERACT_SPECIFIC = new CopyOnWriteArrayList<>();

    private final Player player;
    private final InteractionHand hand;
    private boolean canceled;
    private InteractionResult cancellationResult = InteractionResult.PASS;

    protected PlayerInteractEvent(Player player, InteractionHand hand) {
        this.player = player;
        this.hand = hand;
    }

    public Player getEntity() {
        return this.player;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public ItemStack getItemStack() {
        return this.player.getItemInHand(this.hand);
    }

    public Level getLevel() {
        return this.player.level();
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public InteractionResult getCancellationResult() {
        return this.cancellationResult;
    }

    public void setCancellationResult(InteractionResult result) {
        this.cancellationResult = result;
    }

    public static class EntityInteract extends PlayerInteractEvent {
        private final Entity target;

        public EntityInteract(Player player, InteractionHand hand, Entity target) {
            super(player, hand);
            this.target = target;
        }

        public Entity getTarget() {
            return this.target;
        }
    }

    public static class EntityInteractSpecific extends PlayerInteractEvent {
        private final Entity target;
        private final Vec3 localPos;

        public EntityInteractSpecific(Player player, InteractionHand hand, Entity target, Vec3 localPos) {
            super(player, hand);
            this.target = target;
            this.localPos = localPos;
        }

        public Entity getTarget() {
            return this.target;
        }

        public Vec3 getLocalPos() {
            return this.localPos;
        }
    }

    public static InteractionResult dispatch(Player player, InteractionHand hand, Entity target, Vec3 hitLocation) {
        PlayerInteractEvent event;
        if (hitLocation != null) {
            EntityInteractSpecific e = new EntityInteractSpecific(player, hand, target, hitLocation.subtract(target.position()));
            for (Consumer<EntityInteractSpecific> l : ENTITY_INTERACT_SPECIFIC) {
                l.accept(e);
            }
            event = e;
        } else {
            EntityInteract e = new EntityInteract(player, hand, target);
            for (Consumer<EntityInteract> l : ENTITY_INTERACT) {
                l.accept(e);
            }
            event = e;
        }
        if (event.isCanceled()) {
            return event.getCancellationResult() == InteractionResult.PASS ? InteractionResult.FAIL : event.getCancellationResult();
        }
        return InteractionResult.PASS;
    }
}
