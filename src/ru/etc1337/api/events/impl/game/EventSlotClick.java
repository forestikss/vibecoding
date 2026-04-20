package ru.etc1337.api.events.impl.game;

import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import ru.etc1337.api.events.Event;

public class EventSlotClick extends Event {
    private final Slot dRk;
    private final ClickType dRl;

    public EventSlotClick(Slot zo2, ClickType clickType) {
        this.dRk = zo2;
        this.dRl = clickType;
    }

    public Slot aDf() {
        return this.dRk;
    }

    public ClickType Wp() {
        return this.dRl;
    }
}