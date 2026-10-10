package com.atsuishio.portlib.forge.network;

import net.minecraft.resources.Identifier;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NetworkRegistry {
    private NetworkRegistry() {
    }

    public static SimpleChannel newSimpleChannel(Identifier name, Supplier<String> version, Predicate<String> client, Predicate<String> server) {
        return new SimpleChannel(name);
    }

    public static ChannelBuilder named(Identifier name) {
        return new ChannelBuilder(name);
    }

    public static final class ChannelBuilder {
        private final Identifier name;

        ChannelBuilder(Identifier name) {
            this.name = name;
        }

        public static ChannelBuilder named(Identifier name) {
            return new ChannelBuilder(name);
        }

        public ChannelBuilder networkProtocolVersion(Supplier<String> v) {
            return this;
        }

        public ChannelBuilder clientAcceptedVersions(Predicate<String> p) {
            return this;
        }

        public ChannelBuilder serverAcceptedVersions(Predicate<String> p) {
            return this;
        }

        public SimpleChannel simpleChannel() {
            return new SimpleChannel(name);
        }
    }
}
