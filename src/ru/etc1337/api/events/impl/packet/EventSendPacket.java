package ru.etc1337.api.events.impl.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.IPacket;
import ru.etc1337.api.events.Event;

@Getter
@AllArgsConstructor
public class EventSendPacket extends Event {
	private final IPacket packet;
}
