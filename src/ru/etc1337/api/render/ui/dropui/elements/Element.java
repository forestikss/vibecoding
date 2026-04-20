package ru.etc1337.api.render.ui.dropui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import ru.etc1337.api.interfaces.QuickImports;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public abstract class Element implements QuickImports {
    float defaultHeight = 16, offset = 3;

    public float x, y, width, height, nextHeight;
    protected final List<Element> elements = new ArrayList<>();

    public float getHeight() {
        if (!isVisible()) {
            return 0;
        }
        return this instanceof ElementModule ? height + nextHeight : (height + nextHeight) - offset;
    }

    public boolean collided(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public boolean collided(final float mouseX, final float mouseY, double posX, double posY, float width, float height) {
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }

    protected boolean isHovered(float mouseX, float mouseY) {
        return (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);
    }

    public abstract void draw(MatrixStack stack, float mouseX, float mouseY);

    public void mouseClicked(float x, float y, int button) {}

    public boolean isVisible() {
        return true;
    }

    public void mouseReleased(float x, float y, int button) {}

    public void keyPressed(int keyCode, int scanCode, int modifiers) {}
}