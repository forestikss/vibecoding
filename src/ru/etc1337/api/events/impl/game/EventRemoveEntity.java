package ru.etc1337.api.events.impl.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import ru.etc1337.api.events.Event;

@Getter
@RequiredArgsConstructor
public class EventRemoveEntity extends Event {
    private final Entity entity;
}
