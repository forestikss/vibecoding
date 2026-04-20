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
import ru.etc1337.api.settings.impl.ModeSetting;

import java.util.ArrayList;
import java.util.List;

public class ModeRenderer {
    @Getter
    private final ModeSetting setting;
    private final List<ModeComponent> modeComponents = new ArrayList<>();
    private final float height;

    public ModeRenderer(ModeSetting setting) {
        this.setting = setting;
        for (String mode : setting.getModes()) {
            ModeComponent component = new ModeComponent(mode, this);
            modeComponents.add(component);
            component.init();
        }
        height = Fonts.SEMIBOLD_13.height() - 3F;
    }

    public void render(MatrixStack matrixStack, float x, float y, float moduleWidth, float offset, float mouseX, float mouseY) {
        String string = "Настройка: " + setting.getName();
        Round.draw(matrixStack, new Rect(x + 1.5F, y - height / 2F + 0.5F + offset, Fonts.SEMIBOLD_13.width(string) +3F, height * 2), 1,
                TempColor.getClientColor().alpha(250));
        Fonts.SEMIBOLD_13.draw(matrixStack, string,
                x + 3F, y - height / 2F + offset,
                FixColor.WHITE.getRGB());

        float offsetX = 0, offsetY = 0;
        float maxWidth = 90;
        float spaceX = 5;
        float spaceY = 3;

        for (ModeComponent component : modeComponents) {
            if (offsetX + Fonts.SEMIBOLD_13.width(component.getMode()) > maxWidth) {
                offsetX = 0;
                offsetY += component.getHeight() + spaceY;
            }

            component.render(matrixStack, x + 3 + offsetX, y + height + 4 + offset + offsetY, mouseX, mouseY);
            offsetX += component.getWidth() + spaceX;
        }
    }

    public void mouseClicked(float mouseX, float mouseY, int button) {
        for (ModeComponent component : modeComponents) {
            component.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void keyPressed(int keyCode) {
        for (ModeComponent component : modeComponents) {
            component.keyPressed(keyCode);
        }
    }

    public float getNextHeight(float offset) {
        float offsetY = 0;
        float offsetX = 0;
        float maxWidth = 90;
        float spaceX = 5;
        float spaceY = 3;

        for (ModeComponent component : modeComponents) {
            if (offsetX + Fonts.SEMIBOLD_13.width(component.getMode()) > maxWidth) {
                offsetX = 0;
                offsetY += component.getHeight() + spaceY;
            }
            offsetX += component.getWidth() + spaceX;
        }
        return offsetY + 8 + offset;
    }

    private static class ModeComponent {
        @Getter
        private final String mode;
        private final ModeRenderer renderer;
        private final ColorAnimation colorAnimation = new ColorAnimation(150);
        private final ColorAnimation hoverAnimation = new ColorAnimation(1);
        private float x;
        private float y;
        @Getter
        private float width;
        @Getter
        private float height;
        private boolean binding;

        public ModeComponent(String mode, ModeRenderer renderer) {
            this.mode = mode;
            this.renderer = renderer;
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

            boolean isHovered = Hover.isHovered(x, y, width, height, mouseX, mouseY);
            hoverAnimation.update(isHovered ? TempColor.getClientColor().brighter() : FixColor.BLACK);
            colorAnimation.update(mode.equals(renderer.setting.getCurrentMode()) ? TempColor.getClientColor() : hoverAnimation.getColor());

            float alpha = 55;
            Round.draw(matrixStack, new Rect(x - 1.5f, y, width, height + 1), 3, colorAnimation.getColor().alpha(alpha));
            Fonts.SEMIBOLD_13.draw(matrixStack, text, x, y - 0.5F, FixColor.WHITE.getRGB());
        }

        public void mouseClicked(double mouseX, double mouseY, int button) {
            boolean hoveringClickableArea = Hover.isHovered(x, y, width, height, mouseX, mouseY);
            if (hoveringClickableArea && button == 0) {
                renderer.setting.setCurrentMode(mode);
            }
            if (hoveringClickableArea && button == 2) {
                binding = true;
            }
            if (binding && button != 2) {
                renderer.setting.setModeKey(mode, button);
                binding = false;
            }
        }

        public void keyPressed(int keyCode) {
            if (binding) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                    renderer.setting.setModeKey(mode, -1);
                } else {
                    renderer.setting.setModeKey(mode, keyCode);
                }
                binding = false;
            }
        }
    }
}