package ru.etc1337.api.events.impl.game;

import ru.etc1337.api.events.Event;

public class EventMoveFix extends Event {
    private float yaw, pitch;

    public EventMoveFix(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
}
