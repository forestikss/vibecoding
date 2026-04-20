package ru.etc1337.api.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import ru.etc1337.api.events.Event;

@Getter
@AllArgsConstructor
public class EventEntityRayTrace extends Event {
    private final Entity entity;
}