package com.atsuishio.portlib.forge;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class DistExecutor {
    private DistExecutor() {
    }

    private static boolean on(EnvType env) {
        return FabricLoader.getInstance().getEnvironmentType() == env;
    }

    public static void unsafeRunWhenOn(EnvType env, Supplier<Runnable> toRun) {
        if (on(env)) {
            toRun.get().run();
        }
    }

    public static void safeRunWhenOn(EnvType env, Supplier<Runnable> toRun) {
        unsafeRunWhenOn(env, toRun);
    }

    public static <T> T unsafeCallWhenOn(EnvType env, Supplier<Callable<T>> toRun) {
        if (on(env)) {
            try {
                return toRun.get().call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static <T> T safeCallWhenOn(EnvType env, Supplier<Callable<T>> toRun) {
        return unsafeCallWhenOn(env, toRun);
    }

    public static <T> T unsafeRunForDist(Supplier<Supplier<T>> client, Supplier<Supplier<T>> server) {
        return on(EnvType.CLIENT) ? client.get().get() : server.get().get();
    }

    public static <T> T safeRunForDist(Supplier<Supplier<T>> client, Supplier<Supplier<T>> server) {
        return unsafeRunForDist(client, server);
    }
}
