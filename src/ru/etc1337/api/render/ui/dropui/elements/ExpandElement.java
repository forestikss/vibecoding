package ru.etc1337.api.render.ui.dropui.elements;

public abstract class ExpandElement extends Element {
    private boolean expanded;

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            onPress(mouseX, mouseY, button);
            if (canExpand() && button == 1)
                expanded = !expanded;
        }
        if (isExpanded()) {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    public abstract boolean canExpand();

    public abstract int getHeightWithExpand();

    public abstract void onPress(float mouseX, float mouseY, int button);
}