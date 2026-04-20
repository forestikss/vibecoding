package ru.etc1337.api.render.ui.dropui.elements.extended;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.ColorAnimation;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.game.Maths;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.render.ui.dropui.AnimationMath;
import ru.etc1337.api.settings.impl.SliderSetting;

public class SliderRenderer {
    private boolean dragging;
    private float lerp;
    private final ColorAnimation backgroundAnimation = new ColorAnimation(150);
    @Getter
    private final SliderSetting setting;

    public SliderRenderer(SliderSetting setting) {
        this.setting = setting;
    }

    public void render(MatrixStack stack, float x, float y, float moduleWidth, float mouseX, float mouseY) {
        FixColor fontColor = TempColor.getFontColor();
        float y1 = y + 1;
        float sliderWidth = moduleWidth - 8.5F;
        float amount = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin()) * 0.995f;

        lerp = AnimationMath.fast(lerp, amount, 15f);

        boolean isHovered = isHovered(x, y, sliderWidth, mouseX, mouseY);
        backgroundAnimation.update(isHovered ? TempColor.getClientColor().alpha(55d) : TempColor.getBackgroundColor());

        if (isHovered && dragging) {
            float newValue = (mouseX - 2 - x) / sliderWidth * (setting.getMax() - setting.getMin()) + setting.getMin();
            setting.setCurrentValue((float) Maths.roundHalfUp(newValue, setting.getStep()));
        }

        setting.setCurrentValue((float) MathHelper.clamp(setting.getValue(), setting.getMin(), setting.getMax()));

        Round.draw(stack, new Rect(x + 2F, y1 + 6, sliderWidth, 2), 1, backgroundAnimation.getColor().alpha(125));

        float alpha = 100;
        Round.draw(stack, new Rect(x + 2F, y1 + 6, sliderWidth * lerp, 2), 1,
                TempColor.getClientColor().alpha(alpha), TempColor.getClientColor(),
                TempColor.getClientColor().alpha(alpha), TempColor.getClientColor());
        Round.draw(stack, new Rect(x + 2 + (sliderWidth - 2) * lerp, y1 + 5.5F, 3, 3), 1,
                TempColor.getClientColor().alpha(alpha), TempColor.getClientColor(),
                TempColor.getClientColor().alpha(alpha), TempColor.getClientColor());

        float textY = y1 - (float) Fonts.SEMIBOLD_12.height() / 2;
        String valueStr = String.valueOf(setting.getValue());
        Fonts.SEMIBOLD_12.draw(stack, setting.getName(), x + 2, textY, fontColor.getRGB());
        Fonts.SEMIBOLD_12.draw(stack, valueStr,
                x + sliderWidth - Fonts.SEMIBOLD_12.width(valueStr) + 2.5F,
                textY, fontColor.getRGB());
    }

    public void mouseClicked(float x, float y, float moduleWidth, float mouseX, float mouseY, int mouseButton) {
        float sliderWidth = moduleWidth - 8.5F;
        if (Hover.isHovered(x + 2, y + 3.5F, sliderWidth, 6, mouseX, mouseY) && mouseButton == 0) {
            dragging = true;
        }
    }

    public void mouseReleased(float mouseX, float mouseY, int mouseButton) {
        dragging = false;
    }

    public float getNextHeight() {
        return 1;
    }

    public static boolean isHovered(float x, float y, float sliderWidth, float mouseX, float mouseY) {
        return Hover.isHovered(x + 2, y + 3.5F, sliderWidth, 6, mouseX, mouseY);
    }
}