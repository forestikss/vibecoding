package ru.etc1337.api.draggable;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class AutoDockingConnection {
    @Expose
    private final String targetName;
    @Expose
    private final DockingSide side;
    @Expose
    private final float offsetX;
    @Expose
    private final float offsetY;
    
    public AutoDockingConnection(String targetName, DockingSide side, float offsetX, float offsetY) {
        this.targetName = targetName;
        this.side = side;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
}