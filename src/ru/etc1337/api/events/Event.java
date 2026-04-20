package ru.etc1337.api.events;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import ru.etc1337.Client;

@Getter @Setter
public abstract class Event {
    private boolean cancelled = false;

    public <T extends Event> T hookRender() {
        RenderSystem.runAsFancy(() -> Client.getEventManager().call(this));
        return (T) this;
    }

    public <T extends Event> T hook() {
        Client.getEventManager().call(this);
        return (T) this;
    }
}