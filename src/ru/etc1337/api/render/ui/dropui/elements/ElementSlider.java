package ru.etc1337.api.render.ui.dropui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.api.render.ui.dropui.elements.extended.SliderRenderer;
import ru.etc1337.api.settings.impl.SliderSetting;

public class ElementSlider extends Element {
    public ElementModule module;
    public SliderSetting setting;
    private final SliderRenderer renderer;

    public ElementSlider(ElementModule e, SliderSetting setting) {
        this.module = e;
        this.setting = setting;
        this.renderer = new SliderRenderer(setting);
        setHeight(defaultHeight);
    }

    @Override
    public void draw(MatrixStack stack, float mouseX, float mouseY) {
        renderer.render(stack, x, y, module.width, mouseX, mouseY);
        setNextHeight(renderer.getNextHeight());
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