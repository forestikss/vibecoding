package ru.etc1337.api.events.impl.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.etc1337.api.events.Event;

@AllArgsConstructor
@Getter
public class EventKeyboardClick extends Event {
    int key, scancode;
    boolean released;
}
