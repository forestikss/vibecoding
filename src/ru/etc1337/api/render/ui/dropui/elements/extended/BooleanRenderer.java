package ru.etc1337.api.render.ui.dropui.elements.extended;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.ColorAnimation;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.fonts.impl.CFontRenderer;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.BooleanSetting;

public class BooleanRenderer {
    private final ColorAnimation colorAnimation = new ColorAnimation(150);
    private final ColorAnimation checkMarkAnimation = new ColorAnimation(150);
    private final ColorAnimation borderAnimation = new ColorAnimation(150);

    private static final float CHECKBOX_SIZE = 8f;
    private static final float CHECKBOX_RADIUS = 2f;

    private boolean binding;

    @Getter
    private final BooleanSetting setting;

    public BooleanRenderer(BooleanSetting setting) {
        this.setting = setting;
    }

    public void render(MatrixStack stack, float x, float y, float mouseX, float mouseY) {
        boolean isHovered = isHovered(x, y, mouseX, mouseY);
        colorAnimation.update(setting.isEnabled() ? TempColor.getClientColor() : TempColor.getBackgroundColor());
        borderAnimation.update(isHovered ? TempColor.getClientColor() : FixColor.BLACK);
        FixColor currentColor = colorAnimation.getColor();
        if (!binding) {
            Round.draw(stack, new Rect(x - 1, y - 1, CHECKBOX_SIZE + 2, CHECKBOX_SIZE + 2),
                    CHECKBOX_RADIUS, borderAnimation.getColor().alpha(100));
            Round.draw(stack, new Rect(x, y, CHECKBOX_SIZE, CHECKBOX_SIZE),
                    CHECKBOX_RADIUS, currentColor.alpha(225));
        }

        String text = "waiting key";
        CFontRenderer fontRenderer = binding ? Fonts.SEMIBOLD_12 : Fonts.DREAMCORE_12;
        checkMarkAnimation.update(binding || setting.isEnabled() ? TempColor.getFontColor().alpha(155) : FixColor.BLACK.alpha(55));
        if (binding) {
            fontRenderer.draw(stack, text, x + CHECKBOX_SIZE - fontRenderer.width(text), y + 0.5F,
                    checkMarkAnimation.getColor().getRGB());
        }
    }

    public boolean isHovered(float checkboxX, float checkboxY, float mouseX, float mouseY) {
        return mouseX >= checkboxX && mouseX <= checkboxX + CHECKBOX_SIZE &&
                mouseY >= checkboxY && mouseY <= checkboxY + CHECKBOX_SIZE;
    }

    public void keyPressed(int keyCode) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                setting.setKey(-1);
            } else {
                setting.setKey(keyCode);
            }
            binding = false;
        }
    }
    public void mouseClicked(int key, float checkboxX, float checkboxY, float mouseX, float mouseY) {
        boolean hoveringClickableArea = Hover.isHovered(new Rect(checkboxX, checkboxY, 8, 8), mouseX, mouseY);
        if (hoveringClickableArea && key == 0) {
            setting.toggle();
        }
        if (hoveringClickableArea && key == 2) {
            binding = true;
        }

        if (binding && key != 2) {
            setting.setKey(key);
            binding = false;
        }
    }
}