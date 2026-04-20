package ru.etc1337.api.render;

import lombok.experimental.UtilityClass;
import ru.etc1337.api.draggable.Draggable;

@UtilityClass
public class Hover {
    public boolean isHovered(Draggable draggable, double mouseX, double mouseY) {
        if (draggable == null) return false;
        return mouseX >= draggable.getX() && mouseY >= draggable.getY() && mouseX < draggable.getX() + draggable.getWidth() && mouseY < draggable.getY() + draggable.getHeight();
    }

    public boolean isHovered(Rect rect, double mouseX, double mouseY) {
        if (rect == null) return false;
        return mouseX >= rect.getX() && mouseY >= rect.getY() && mouseX < rect.getX() + rect.getWidth() && mouseY < rect.getY() + rect.getHeight();
    }

    public boolean isHovered(final double x, final double y, final double width, final double height, final int mouseX, final int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public boolean isHovered(final double x, final double y, final double width, final double height, final double mouseX, final double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}