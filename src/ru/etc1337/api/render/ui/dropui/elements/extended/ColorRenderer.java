package ru.etc1337.api.render.ui.dropui.elements.extended;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.ColorAnimation;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.ColorSetting;

import java.awt.*;

public class ColorRenderer {
    @Getter
    private final ColorSetting setting;
    private float hue;
    private boolean draggingHue;
    private boolean draggingColor;
    private boolean colorPickerOpen;
    private final float defaultHeight = 16;
    private final ColorAnimation previewBorderAnimation = new ColorAnimation(150);

    public ColorRenderer(ColorSetting setting) {
        this.setting = setting;
        this.hue = Color.RGBtoHSB(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), null)[0];
    }

    public void render(MatrixStack stack, float x, float y, float moduleWidth, float mouseX, float mouseY) {
        float[] hsb = Color.RGBtoHSB(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), null);

        Fonts.SEMIBOLD_13.draw(stack, setting.getName(), x + 2, y, TempColor.getFontColor().getRGB());

        float previewSize = 10;
        float previewX = x + moduleWidth - previewSize - 5;
        float previewY = y;
        boolean isPreviewHovered = Hover.isHovered(previewX, previewY, previewSize, previewSize, mouseX, mouseY);
        previewBorderAnimation.update(isPreviewHovered ? TempColor.getClientColor() : TempColor.getClientColor().alpha(155));
        Round.draw(stack, new Rect(previewX - 1, previewY - 1, previewSize + 2, previewSize + 2), 2, previewBorderAnimation.getColor().alpha(100));
        Round.draw(stack, new Rect(previewX, previewY, previewSize, previewSize), 2, setting.getColor());

        if (colorPickerOpen) {
            float colorPickerX = x + 2;
            float colorPickerY = y + defaultHeight - 2;
            float colorPickerWidth = moduleWidth - 20;
            float colorPickerHeight = 40;

            drawColorPickerRect(stack, colorPickerX, colorPickerY, colorPickerWidth, colorPickerHeight);

            float hueSliderX = previewX + 1;
            float hueSliderY = colorPickerY;
            float hueSliderWidth = 8;
            float hueSliderHeight = colorPickerHeight;

            drawHueSlider(stack, hueSliderX, hueSliderY, hueSliderWidth, hueSliderHeight);

            float indicatorX = colorPickerX + hsb[1] * colorPickerWidth;
            float indicatorY = colorPickerY + (1 - hsb[2]) * colorPickerHeight;
            Round.drawRoundCircle(stack, indicatorX, indicatorY + 2, 3, FixColor.BLACK);
            Round.drawRoundCircle(stack, indicatorX, indicatorY + 2, 2, setting.getColor());

            if (draggingHue || draggingColor) {
                updateColor(mouseX, mouseY, colorPickerX, colorPickerY, colorPickerWidth, colorPickerHeight,
                        hueSliderX, hueSliderY, hueSliderHeight);
            }
        }
    }

    public void mouseClicked(float x, float y, float moduleWidth, float mouseX, float mouseY, int mouseButton) {
        float previewSize = 10;
        float previewX = x + moduleWidth - previewSize - 5;
        float previewY = y;

        if (Hover.isHovered(previewX, previewY, previewSize, previewSize, mouseX, mouseY) && mouseButton == 0) {
            colorPickerOpen = !colorPickerOpen;
            return;
        }

        if (colorPickerOpen && mouseButton == 0) {
            float colorPickerX = x + 2;
            float colorPickerY = y + defaultHeight - 2;
            float colorPickerWidth = moduleWidth - 20;
            float colorPickerHeight = 40;

            float hueSliderX = previewX + 1;
            float hueSliderY = colorPickerY;
            float hueSliderWidth = 8;
            float hueSliderHeight = colorPickerHeight;

            if (Hover.isHovered(hueSliderX, hueSliderY, hueSliderWidth, hueSliderHeight, mouseX, mouseY)) {
                draggingHue = true;
                hue = MathHelper.clamp((mouseY - hueSliderY) / hueSliderHeight, 0, 1);
                setting.setColor(Color.getHSBColor(hue, 1, 1).getRGB());
            } else if (Hover.isHovered(colorPickerX, colorPickerY, colorPickerWidth, colorPickerHeight, mouseX, mouseY)) {
                draggingColor = true;
                updateColor(mouseX, mouseY, colorPickerX, colorPickerY, colorPickerWidth, colorPickerHeight,
                        hueSliderX, hueSliderY, hueSliderHeight);
            }
        }
    }

    public void mouseReleased(float mouseX, float mouseY, int mouseButton) {
        draggingHue = false;
        draggingColor = false;
    }

    public float getHeight() {
        return colorPickerOpen ? defaultHeight + 40 + 8 : defaultHeight;
    }

    private void drawColorPickerRect(MatrixStack stack, float x, float y, float width, float height) {
        FixColor hueColor = new FixColor(Color.HSBtoRGB(hue, 1, 1));
        Round.draw(stack, new Rect(x, y, width, height), 2,
                FixColor.WHITE, hueColor, hueColor, FixColor.WHITE);

        FixColor transparentBlack = new FixColor(0, 0, 0, 0);
        Round.draw(stack, new Rect(x, y, width, height), 2,
                transparentBlack, transparentBlack, FixColor.BLACK, FixColor.BLACK);
    }

    private void drawHueSlider(MatrixStack stack, float x, float y, float width, float height) {
        float segmentHeight = height / 3f;

        Round.draw(stack,
                new Rect(x, y, width, segmentHeight),
                2, 2, 0, 0,
                new FixColor(Color.HSBtoRGB(0f, 1, 1)),
                new FixColor(Color.HSBtoRGB(0f, 1, 1)),
                new FixColor(Color.HSBtoRGB(0.33f, 1, 1)),
                new FixColor(Color.HSBtoRGB(0.33f, 1, 1))
        );

        Round.draw(stack,
                new Rect(x, y + segmentHeight, width, segmentHeight),
                0,
                new FixColor(Color.HSBtoRGB(0.33f, 1, 1)),
                new FixColor(Color.HSBtoRGB(0.33f, 1, 1)),
                new FixColor(Color.HSBtoRGB(0.66f, 1, 1)),
                new FixColor(Color.HSBtoRGB(0.66f, 1, 1))
        );

        Round.draw(stack,
                new Rect(x, y + 2*segmentHeight, width, height - 2*segmentHeight),
                0, 0, 2, 2,
                new FixColor(Color.HSBtoRGB(0.66f, 1, 1)),
                new FixColor(Color.HSBtoRGB(0.66f, 1, 1)),
                new FixColor(Color.HSBtoRGB(1f, 1, 1)),
                new FixColor(Color.HSBtoRGB(1f, 1, 1))
        );

        float indicatorY = y + hue * height;
        Round.drawRoundCircle(stack,
                x + width/2f, indicatorY, 3,
                FixColor.WHITE
        );
    }

    private void updateColor(float mouseX, float mouseY, float colorPickerX, float colorPickerY,
                             float colorPickerWidth, float colorPickerHeight,
                             float hueSliderX, float hueSliderY, float hueSliderHeight) {
        if (draggingHue) {
            hue = MathHelper.clamp((mouseY - hueSliderY) / hueSliderHeight, 0, 1);
            float[] hsb = Color.RGBtoHSB(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), null);
            setting.setColor(Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB());
        } else if (draggingColor) {
            float saturation = MathHelper.clamp((mouseX - colorPickerX) / colorPickerWidth, 0, 1);
            float brightness = MathHelper.clamp(1 - (mouseY - colorPickerY) / colorPickerHeight, 0, 1);
            setting.setColor(Color.getHSBColor(hue, saturation, brightness).getRGB());
        }
    }
}