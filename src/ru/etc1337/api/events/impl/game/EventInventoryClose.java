package ru.etc1337.api.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import ru.etc1337.api.events.Event;

@AllArgsConstructor
@Getter
public class EventInventoryClose extends Event {
    private Screen screen;
    private int windowId;
}
