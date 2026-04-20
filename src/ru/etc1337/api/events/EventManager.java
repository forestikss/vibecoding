package ru.etc1337.api.events;

import ru.etc1337.api.events.handler.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    public static boolean blocked = false;
    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    public void register(EventListener listener) {
        listeners.add(listener);
    }

    public void unregister(EventListener listener) {
        listeners.remove(listener);
    }

    public void call(Event event) {
        if (blocked) return;
        listeners.forEach(listener -> {
            listener.onEvent(event);
        });
    }
}