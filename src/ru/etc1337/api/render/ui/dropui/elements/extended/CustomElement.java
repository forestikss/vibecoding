package ru.etc1337.api.render.ui.dropui.elements.extended;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import ru.etc1337.api.render.Hover;

@Getter
@Setter
@SuppressWarnings("all")
public class CustomElement {

    public float x, y, width, height;
    private boolean isDisplayingElement;

    public void init() {

    }

    public void render(MatrixStack matrixStack, float mouseX, float mouseY) {
    }


    public void mouseClicked(double mouseX, double mouseY, int button) {

    }


    public void mouseReleased(double mouseX, double mouseY, int button) {

    }


    public void keyPressed(int keyCode, int scanCode, int modifiers) {

    }

    public void charTyped(char codePoint, int modifiers) {

    }
}