package com.atsuishio.portlib.forge.client;

import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ScreenOpeningEvent {
    public static final List<Consumer<ScreenOpeningEvent>> LISTENERS = new CopyOnWriteArrayList<>();

    private final Screen currentScreen;
    private final Screen newScreen;
    private boolean canceled;

    public ScreenOpeningEvent(Screen currentScreen, Screen newScreen) {
        this.currentScreen = currentScreen;
        this.newScreen = newScreen;
    }

    public Screen getCurrentScreen() {
        return this.currentScreen;
    }

    public Screen getNewScreen() {
        return this.newScreen;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public static boolean post(Screen current, Screen next) {
        if (LISTENERS.isEmpty() || next == null) {
            return false;
        }
        ScreenOpeningEvent event = new ScreenOpeningEvent(current, next);
        for (Consumer<ScreenOpeningEvent> l : LISTENERS) {
            l.accept(event);
        }
        return event.isCanceled();
    }
}
