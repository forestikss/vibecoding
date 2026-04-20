package ru.etc1337.api.render.ui.dropui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.api.render.ui.dropui.elements.extended.BindRenderer;
import ru.etc1337.api.settings.impl.BindSetting;

public class ElementBind extends Element {
    public ElementModule module;
    public BindSetting setting;
    private final BindRenderer renderer;

    public ElementBind(ElementModule e, BindSetting setting) {
        this.module = e;
        this.setting = setting;
        this.renderer = new BindRenderer(setting);
        setHeight(defaultHeight);
    }

    @Override
    public void draw(MatrixStack stack, float mouseX, float mouseY) {
        renderer.render(stack, x, y, module.width, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int mouseButton) {
        renderer.mouseClicked(x, y, module.width, mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        renderer.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isVisible() {
        return setting.getHideCondition() == null || setting.getHideCondition().get();
    }
}