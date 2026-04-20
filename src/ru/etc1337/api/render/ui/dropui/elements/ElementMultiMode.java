package ru.etc1337.api.render.ui.dropui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.api.render.ui.dropui.elements.extended.MultiModeRenderer;
import ru.etc1337.api.settings.impl.MultiModeSetting;

public class ElementMultiMode extends Element {
    public MultiModeSetting setting;
    public ElementModule module;
    private final MultiModeRenderer renderer;

    public ElementMultiMode(ElementModule e, MultiModeSetting setting) {
        this.module = e;
        this.setting = setting;
        this.renderer = new MultiModeRenderer(setting);
        setHeight(defaultHeight);
    }

    @Override
    public void draw(MatrixStack matrixStack, float mouseX, float mouseY) {
        renderer.render(matrixStack, x, y, module.width, offset, mouseX, mouseY);
        setNextHeight(renderer.getNextHeight(offset));
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        renderer.keyPressed(keyCode);
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        renderer.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isVisible() {
        return setting.getHideCondition() == null || setting.getHideCondition().get();
    }
}