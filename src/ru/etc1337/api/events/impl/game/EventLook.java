package ru.etc1337.api.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.etc1337.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class EventLook extends Event {
    private double yaw, pitch;
}