package ru.etc1337.api.events.impl.game;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.events.Event;

public class EventMove extends Event {
    private Vector3d from, to, motion;
    private boolean toGround;
    private AxisAlignedBB aabbFrom;
    private boolean ignoreHorizontal, ignoreVertical, collidedHorizontal, collidedVertical;

    public EventMove(Vector3d from, Vector3d to, Vector3d motion, boolean toGround, AxisAlignedBB aabbFrom,
                     boolean ignoreHorizontal, boolean ignoreVertical, boolean collidedHorizontal, boolean collidedVertical) {
        this.from = from;
        this.to = to;
        this.motion = motion;
        this.toGround = toGround;
        this.aabbFrom = aabbFrom;
        this.ignoreHorizontal = ignoreHorizontal;
        this.ignoreVertical = ignoreVertical;
        this.collidedHorizontal = collidedHorizontal;
        this.collidedVertical = collidedVertical;
    }

    public Vector3d getFrom() { return from; }
    public void setFrom(Vector3d from) { this.from = from; }

    public Vector3d getTo() { return to; }
    public void setTo(Vector3d to) { this.to = to; }

    public Vector3d getMotion() { return motion; }
    public void setMotion(Vector3d motion) { this.motion = motion; }

    public boolean isToGround() { return toGround; }
    public void setToGround(boolean toGround) { this.toGround = toGround; }

    public AxisAlignedBB getAabbFrom() { return aabbFrom; }
    public void setAabbFrom(AxisAlignedBB aabbFrom) { this.aabbFrom = aabbFrom; }

    public boolean isIgnoreHorizontal() { return ignoreHorizontal; }
    public void setIgnoreHorizontal(boolean ignoreHorizontal) { this.ignoreHorizontal = ignoreHorizontal; }

    public boolean isIgnoreVertical() { return ignoreVertical; }
    public void setIgnoreVertical(boolean ignoreVertical) { this.ignoreVertical = ignoreVertical; }

    public boolean isCollidedHorizontal() { return collidedHorizontal; }
    public void setCollidedHorizontal(boolean collidedHorizontal) { this.collidedHorizontal = collidedHorizontal; }

    public boolean isCollidedVertical() { return collidedVertical; }
    public void setCollidedVertical(boolean collidedVertical) { this.collidedVertical = collidedVertical; }
}
