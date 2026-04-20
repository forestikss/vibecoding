package ru.etc1337.api.render.ui.dropui.elements.extended;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.client.util.InputMappings;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.ColorAnimation;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.BindSetting;

import java.awt.*;

public class BindRenderer {
    @Getter
    private final BindSetting setting;
    private boolean binding;
    private final ColorAnimation backgroundAnimation = new ColorAnimation(150);

    public BindRenderer(BindSetting setting) {
        this.setting = setting;
    }

    public void render(MatrixStack stack, float x, float y, float moduleWidth, float mouseX, float mouseY) {
        Fonts.SEMIBOLD_13.draw(stack, setting.getName(), x + 2, y, new Color(255, 255, 255).getRGB());

        String key = getKeyString(setting.getKey());
        float textWidth = Fonts.SEMIBOLD_13.width(key);
        float totalWidth = textWidth + 8;

        boolean isHovered = Hover.isHovered(x + moduleWidth - totalWidth - 7, y, totalWidth, 10, mouseX, mouseY);
        backgroundAnimation.update(binding ? TempColor.getClientColor().brighter() : (isHovered ? TempColor.getClientColor() : TempColor.getBackgroundColor()));

        Round.draw(stack,
                new Rect(x + moduleWidth - totalWidth - 7, y, totalWidth, 10),
                2,
                backgroundAnimation.getColor().alpha(55)
        );

        Fonts.SEMIBOLD_13.draw(stack,
                key, x + moduleWidth - totalWidth - 7 + (totalWidth - textWidth) / 2, y + (10 - Fonts.SEMIBOLD_13.height()) / 2F - 2,
                new Color(255, 255, 255).getRGB()
        );
    }

    public void mouseClicked(float x, float y, float moduleWidth, float mouseX, float mouseY, int mouseButton) {
        boolean hoveringClickableArea = Hover.isHovered(x, y, moduleWidth, 10, mouseX, mouseY);
        if (hoveringClickableArea && mouseButton == 0) {
            binding = true;
        }

        if (binding && mouseButton != 0) {
            setting.setKey(mouseButton);
            binding = false;
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                setting.setKey(-1);
            } else {
                setting.setKey(keyCode);
            }
            binding = false;
        }
    }

    private String getKeyString(int key) {
        String out = InputMappings.getInputByCode(key, GLFW.GLFW_KEY_UNKNOWN).getTranslationKey().replace("key.keyboard.", "").toUpperCase();
        if (out.length() > 4) {
            out = out.substring(0, 3);
        }
        return key == -1 ? "None" : out;
    }
}