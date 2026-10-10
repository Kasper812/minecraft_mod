package com.atsuishio.portlib.forge.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PacketDistributor<T> {
    public static final PacketDistributor<ServerPlayer> PLAYER = new PacketDistributor<>(p -> List.of(p));
    public static final PacketDistributor<Void> ALL = new PacketDistributor<>(v -> {
        MinecraftServer server = PacketDistributor.server();
        return server == null ? List.of() : new ArrayList<>(server.getPlayerList().getPlayers());
    });
    public static final PacketDistributor<Entity> TRACKING_ENTITY = new PacketDistributor<>(e -> e.level() instanceof ServerLevel ? PlayerLookup.tracking(e) : List.of());
    public static final PacketDistributor<Entity> TRACKING_ENTITY_AND_SELF = new PacketDistributor<>(e -> {
        List<ServerPlayer> list = new ArrayList<>(e.level() instanceof ServerLevel ? PlayerLookup.tracking(e) : List.of());
        if (e instanceof ServerPlayer sp && !list.contains(sp)) {
            list.add(sp);
        }
        return list;
    });
    public static final PacketDistributor<LevelChunk> TRACKING_CHUNK = new PacketDistributor<>(c -> c.getLevel() instanceof ServerLevel sl ? PlayerLookup.tracking(sl, c.getPos()) : List.of());
    public static final PacketDistributor<ResourceKey<Level>> DIMENSION = new PacketDistributor<>(key -> {
        MinecraftServer server = PacketDistributor.server();
        ServerLevel level = server == null ? null : server.getLevel(key);
        return level == null ? List.of() : new ArrayList<>(level.players());
    });
    public static final PacketDistributor<TargetPoint> NEAR = new PacketDistributor<>(tp -> {
        MinecraftServer server = PacketDistributor.server();
        ServerLevel level = server == null ? null : server.getLevel(tp.dim());
        if (level == null) {
            return List.of();
        }
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer p : level.players()) {
            if (p != tp.excluded() && p.distanceToSqr(tp.x(), tp.y(), tp.z()) < tp.r2()) {
                out.add(p);
            }
        }
        return out;
    });
    public static final PacketDistributor<Void> SERVER = new PacketDistributor<>(v -> List.of());

    private static volatile MinecraftServer currentServer;

    private final Function<T, Collection<ServerPlayer>> resolver;

    private PacketDistributor(Function<T, Collection<ServerPlayer>> resolver) {
        this.resolver = resolver;
    }

    public static void setServer(MinecraftServer server) {
        currentServer = server;
    }

    public static MinecraftServer server() {
        return currentServer;
    }

    public PacketTarget with(Supplier<T> supplier) {
        return new PacketTarget(() -> resolver.apply(supplier.get()), this == SERVER);
    }

    public PacketTarget with(T value) {
        return new PacketTarget(() -> resolver.apply(value), this == SERVER);
    }

    public PacketTarget noArg() {
        return new PacketTarget(() -> resolver.apply(null), this == SERVER);
    }

    public record TargetPoint(ServerPlayer excluded, double x, double y, double z, double r2, ResourceKey<Level> dim) {
        public TargetPoint(double x, double y, double z, double r2, ResourceKey<Level> dim) {
            this(null, x, y, z, r2, dim);
        }

        public static Supplier<TargetPoint> p(double x, double y, double z, double r2, ResourceKey<Level> dim) {
            TargetPoint point = new TargetPoint(x, y, z, r2, dim);
            return () -> point;
        }
    }

    public static final class PacketTarget {
        final Supplier<Collection<ServerPlayer>> players;
        final boolean toServer;

        PacketTarget(Supplier<Collection<ServerPlayer>> players, boolean toServer) {
            this.players = players;
            this.toServer = toServer;
        }
    }
}
