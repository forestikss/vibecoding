package ru.etc1337.api.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import ru.etc1337.api.events.Event;

@Getter
@AllArgsConstructor
public class EventTotemPop extends Event {
    private final PlayerEntity entity;
    private int pops;
}
