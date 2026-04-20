package ru.etc1337.api.events.handler;

import ru.etc1337.api.events.Event;

public interface EventListener {
    void onEvent(Event event);
}