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
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;

import java.util.ArrayList;
import java.util.List;

public class MultiModeRenderer {
    @Getter
    private final MultiModeSetting setting;
    private final List<MultiModeComponent> multiModeComponents = new ArrayList<>();
    private final float height;
    
    public MultiModeRenderer(MultiModeSetting setting) {
        this.setting = setting;
        for (BooleanSetting mode : setting.getBoolSettings()) {
            MultiModeComponent component = new MultiModeComponent(mode, this);
            multiModeComponents.add(component);
            component.init();
        }
        height = Fonts.SEMIBOLD_13.height() - 3F;
    }

    public void render(MatrixStack matrixStack, float x, float y, float moduleWidth, float offset, float mouseX, float mouseY) {
        String string = "Настройка: " + setting.getName();
        Round.draw(matrixStack, new Rect(x + 1.5F, y - height / 2F + 0.5F + offset, Fonts.SEMIBOLD_13.width(string) + 3F, height * 2), 1,
                TempColor.getClientColor().alpha(250));
        Fonts.SEMIBOLD_13.draw(matrixStack, string,
                x + 3F, y - height / 2F + offset,
                FixColor.WHITE.getRGB());

        float offsetX = 0, offsetY = 0;
        float maxWidth = 85;
        float spaceX = 5;
        float spaceY = 3;

        for (MultiModeComponent component : multiModeComponents) {
            if (offsetX + Fonts.SEMIBOLD_13.width(component.getMode()) > maxWidth) {
                offsetX = 0;
                offsetY += component.getHeight() + spaceY;
            }

            component.render(matrixStack, x + 3 + offsetX, y + height + 4 + offset + offsetY, mouseX, mouseY);
            offsetX += component.getWidth() + spaceX;
        }
    }

    public void mouseClicked(float mouseX, float mouseY, int button) {
        for (MultiModeComponent component : multiModeComponents) {
            component.mouseClicked(mouseX, mouseY, button);
        }
    }

    public float getNextHeight(float offset) {
        float offsetX = 0, offsetY = 0;
        float maxWidth = 85;
        float spaceX = 5;
        float spaceY = 3;

        for (MultiModeComponent component : multiModeComponents) {
            if (offsetX + Fonts.SEMIBOLD_13.width(component.getMode()) > maxWidth) {
                offsetX = 0;
                offsetY += component.getHeight() + spaceY;
            }
            offsetX += component.getWidth() + spaceX;
        }
        return offsetY + 8 + offset;
    }

    public void keyPressed(int keyCode) {
        for (MultiModeComponent component : multiModeComponents) {
            component.keyPressed(keyCode);
        }
    }

    private static class MultiModeComponent {
        @Getter
        private final BooleanSetting setting;
        @Getter
        private final String mode;
        private final ColorAnimation colorAnimation = new ColorAnimation(150);
        private final ColorAnimation hoverAnimation = new ColorAnimation(1);
        private float x;
        private float y;
        @Getter
        private float width;
        @Getter
        private float height;

        private boolean binding;

        public MultiModeComponent(BooleanSetting setting, MultiModeRenderer renderer) {
            this.setting = setting;
            this.mode = setting.getName();
        }

        public void init() {
            String text = binding ? "waiting key" : mode;
            this.width = Fonts.SEMIBOLD_13.width(text);
            this.height = Fonts.SEMIBOLD_13.height();
        }

        public void render(MatrixStack matrixStack, float x, float y, float mouseX, float mouseY) {
            this.x = x;
            this.y = y;

            String text = binding ? "waiting key" : mode;
            this.width = Fonts.SEMIBOLD_13.width(text);
            this.height = Fonts.SEMIBOLD_13.height();

            float width = Fonts.SEMIBOLD_13.width(text) + 3;
            float height = this.height;

            boolean isHovered = Hover.isHovered(x, y, width, height, mouseX, mouseY);
            hoverAnimation.update(isHovered ? TempColor.getClientColor().brighter() : FixColor.BLACK);
            colorAnimation.update(setting.isEnabled() ? TempColor.getClientColor() : hoverAnimation.getColor());

            float alpha = 55;
            Round.draw(matrixStack, new Rect(x - 1.5f, y, width, height + 1), 3, colorAnimation.getColor().alpha(alpha));
            Fonts.SEMIBOLD_13.draw(matrixStack, text, x, y - 0.5F, FixColor.WHITE.getRGB());
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

        public void mouseClicked(double mouseX, double mouseY, int button) {
            boolean hoveringClickableArea = Hover.isHovered(x, y, width, height, mouseX, mouseY);
            if (hoveringClickableArea && button == 0) {
                setting.toggle();
            }
            if (hoveringClickableArea && button == 2) {
                binding = true;
            }

            if (binding && button != 2) {
                setting.setKey(button);
                binding = false;
            }
        }
    }
}