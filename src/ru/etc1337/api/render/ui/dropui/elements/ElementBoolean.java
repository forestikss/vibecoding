package ru.etc1337.api.render.ui.dropui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.ui.dropui.elements.extended.BooleanRenderer;
import ru.etc1337.api.settings.impl.BooleanSetting;

public class ElementBoolean extends Element {
    public ElementModule module;
    public BooleanSetting setting;
    private final BooleanRenderer booleanRenderer;

    public ElementBoolean(ElementModule e, BooleanSetting setting) {
        this.module = e;
        this.setting = setting;
        this.booleanRenderer = new BooleanRenderer(setting);
        setHeight(defaultHeight);
    }

    @Override
    public void draw(MatrixStack stack, float mouseX, float mouseY) {
        Fonts.SEMIBOLD_13.draw(stack, setting.getName(),
                x + 2, y,
                TempColor.getFontColor().getRGB());

        float checkboxX = x + module.width - 14;
        float checkboxY = y;
        booleanRenderer.render(stack, checkboxX, checkboxY, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int mouseButton) {
        float checkboxX = x + module.width - 14;
        float checkboxY = y;
        booleanRenderer.mouseClicked(mouseButton, checkboxX, checkboxY, mouseX, mouseY);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        booleanRenderer.keyPressed(keyCode);
    }

    @Override
    public boolean isVisible() {
        return setting.getHideCondition() == null || setting.getHideCondition().get();
    }
}