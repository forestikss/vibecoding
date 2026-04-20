package ru.etc1337.api.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.etc1337.api.events.Event;

@Getter
@AllArgsConstructor
public class EventSendMessage extends Event {
    private final String message;
}
