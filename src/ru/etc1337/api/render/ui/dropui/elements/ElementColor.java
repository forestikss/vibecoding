package ru.etc1337.api.render.ui.dropui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.api.render.ui.dropui.elements.extended.ColorRenderer;
import ru.etc1337.api.settings.impl.ColorSetting;

public class ElementColor extends Element {
    public ElementModule module;
    public ColorSetting setting;
    private final ColorRenderer renderer;

    public ElementColor(ElementModule e, ColorSetting setting) {
        this.module = e;
        this.setting = setting;
        this.renderer = new ColorRenderer(setting);
        setHeight(defaultHeight);
    }

    @Override
    public void draw(MatrixStack stack, float mouseX, float mouseY) {
        renderer.render(stack, x, y, module.width, mouseX, mouseY);
        setHeight(renderer.getHeight());
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int mouseButton) {
        renderer.mouseClicked(x, y, module.width, mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int mouseButton) {
        renderer.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isVisible() {
        return setting.getHideCondition() == null || setting.getHideCondition().get();
    }
}