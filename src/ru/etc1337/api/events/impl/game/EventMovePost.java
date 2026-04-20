package ru.etc1337.api.events.impl.game;

import ru.etc1337.api.events.Event;

public class EventMovePost extends Event {
    private double horizontalMove;

    public EventMovePost(double horizontalMove) {
        this.horizontalMove = horizontalMove;
    }

    public double getHorizontalMove() { return horizontalMove; }
    public void setHorizontalMove(double horizontalMove) { this.horizontalMove = horizontalMove; }
}
