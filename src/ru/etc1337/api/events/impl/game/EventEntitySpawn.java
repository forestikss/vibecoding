package ru.etc1337.api.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import ru.etc1337.api.events.Event;

@AllArgsConstructor @Getter
public class EventEntitySpawn extends Event {
	private final Entity entity;
}
