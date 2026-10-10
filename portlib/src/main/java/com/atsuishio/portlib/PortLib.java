package com.atsuishio.portlib;

import com.atsuishio.portlib.forge.network.PacketDistributor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class PortLib implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(PacketDistributor::setServer);
        String dump = System.getProperty("portlib.dumpItems");
        if (dump != null) {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                try (java.io.PrintWriter w = new java.io.PrintWriter(dump)) {
                    for (var e : net.minecraft.core.registries.BuiltInRegistries.ITEM.entrySet()) {
                        var id = e.getKey().identifier();
                        if (id.getNamespace().equals("minecraft")) {
                            continue;
                        }
                        StringBuilder chain = new StringBuilder();
                        for (Class<?> c = e.getValue().getClass(); c != null; c = c.getSuperclass()) {
                            if (chain.length() > 0) {
                                chain.append(',');
                            }
                            chain.append(c.getSimpleName());
                        }
                        for (Class<?> i : e.getValue().getClass().getInterfaces()) {
                            chain.append(',').append(i.getSimpleName());
                        }
                        w.println(id.getNamespace() + " " + id.getPath() + " " + e.getValue().getClass().getName() + " " + (e.getValue() instanceof net.minecraft.world.item.BlockItem) + " " + chain);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Runtime.getRuntime().halt(0);
            });
        }
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PacketDistributor.setServer(null));
    }
}
