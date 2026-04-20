package ru.etc1337.api.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import ru.etc1337.api.events.Event;

@Getter
@AllArgsConstructor
public class EventAttack extends Event {
	private final LivingEntity target;
}
