package ru.etc1337.api.render.ui.dropui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.client.util.InputMappings;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.Setting;
import ru.etc1337.api.settings.impl.*;
import ru.etc1337.client.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class ElementModule extends Element {
    @Getter
    private final Module module;
    public boolean extended, isHoveringModule, binding;
    private boolean visible = true;
    public List<Element> settings = new ArrayList<>();
    private static final float ROUND_RADIUS = 4.0F;
    private static final int BASE_HEIGHT = 16;

    public void setVisible(boolean visible) { this.visible = visible; }

    @Override
    public boolean isVisible() { return visible; }

    public ElementModule(Module module) {
        this.module = module;
        setHeight(BASE_HEIGHT);
        initializeSettings();
    }

    private void initializeSettings() {
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting booleanSetting) {
                settings.add(new ElementBoolean(this, booleanSetting));
            } else if (setting instanceof SliderSetting sliderSetting) {
                settings.add(new ElementSlider(this, sliderSetting));
            } else if (setting instanceof ModeSetting elementMode) {
                settings.add(new ElementMode(this, elementMode));
            } else if (setting instanceof MultiModeSetting multiModeSetting) {
                settings.add(new ElementMultiMode(this, multiModeSetting));
            } else if (setting instanceof BindSetting bindSetting) {
                settings.add(new ElementBind(this, bindSetting));
            } else if (setting instanceof ColorSetting colorSetting) {
                settings.add(new ElementColor(this, colorSetting));
            }
        }
    }

    @Override
    public void draw(MatrixStack stack, float mouseX, float mouseY) {
        isHoveringModule = isHovered(mouseX, mouseY);
        String keyName = GLFW.glfwGetKeyName(module.getKey(), 0);
        String displayName = getDisplayName(keyName);

        FixColor enabledColor = module.isEnabled() ? TempColor.getClientColor() : TempColor.getBackgroundColor();
        drawModuleBackground(stack, enabledColor);
        drawModuleText(stack, displayName);

        if (extended) {
            drawExtendedElements(stack, mouseX, mouseY);
        }
    }

    private String getDisplayName(String keyName) {
        if (binding) {
            return "Press a key.. " + (keyName != null ? keyName : "");
        }
        boolean showKey = module.getKey() != -1 && InputMappings.isKeyDown(window.getHandle(), GLFW.GLFW_KEY_INSERT);
        return module.getName() + (showKey && keyName != null ? " " + keyName : "");
    }

    private void drawModuleBackground(MatrixStack stack, FixColor enabledColor) {
        FixColor color = module.isEnabled() ? enabledColor : FixColor.BLACK;
        float offset = 0;
        if (extended) {
            for (Element e : settings) {
                if (e.isVisible()) {
                    offset += e.getHeight();
                }
            }
        }

        float alpha = 55;
/*
        Rect rect = new Rect(x + 2.5F, y + 1F, width - 5F, 12.0F + offset);
        Render.outline(stack, rect, -0.5f,1F,
                color.alpha(alpha), color.alpha(alpha), color, color);
*/


        float round = ROUND_RADIUS;
        float round2 = extended ? 0 : round;
        Rect rect = new Rect(x + 2.5F, y + 1F, width - 5F, 12.0F + offset);

        Round.draw(stack, rect.height(rect.getHeight() - offset),
                round, round, round2, round2,
                color.alpha(alpha * 2), color.alpha(alpha), color.alpha(alpha * 2), color.alpha(alpha)
        );

        if (extended) {
            Round.draw(stack, rect.y(rect.getY() + 12.0F).height(rect.getHeight() - 12.0F),
                    round2, round2, round, round,
                    color.alpha(alpha)
            );
        }


     /*   Render.outline(stack, rect.width(rect.getWidth() + 0.5F), round, 0,0F,
                color, color, color, color);*/
    }

    private void drawModuleText(MatrixStack stack, String displayName) {
        FixColor color = TempColor.getFontColor();
        Fonts.SEMIBOLD_16.draw(stack, displayName,
                x + 5,
                y + 2,
                module.isEnabled() ? FixColor.WHITE.alpha(155).getRGB() : color.alpha(155).getRGB()
        );

        if (!settings.isEmpty()) {
            Fonts.SEMIBOLD_16.draw(stack, "»",
                    x + width - 10,
                    y + 2,
                    color.getRGB()
            );
        }
    }

    private void drawExtendedElements(MatrixStack stack, float mouseX, float mouseY) {
        float offset = 0;
        for (Element e : settings) {
            if (e.isVisible()) {
                e.x = this.x + 2.5F;
                e.y = this.y + 15 + offset;
                e.width = this.width;
                e.draw(stack, mouseX, mouseY);
                offset += e.getHeight();
            }
        }
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int mouseButton) {
        if (binding && mouseButton != 0) {
            module.setKey(mouseButton);
            binding = false;
            return;
        }

        if (isHovered(mouseX, mouseY)) {
            handleMouseClick(mouseButton);
        }

        if (extended) {
            settings.stream()
                    .filter(Element::isVisible)
                    .forEach(e -> e.mouseClicked(mouseX, mouseY, mouseButton));
        }
    }

    private void handleMouseClick(int mouseButton) {
        switch (mouseButton) {
            case 0:
                module.toggle();
                break;
            case 1:
                if (!module.getSettings().isEmpty()) {
                    extended = !extended;
                }
                break;
            case 2:
                binding = true;
                break;
        }
    }

    @Override
    public void mouseReleased(float x, float y, int button) {
        if (extended) {
            settings.stream()
                    .filter(Element::isVisible)
                    .forEach(e -> e.mouseReleased(x, y, button));
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            module.setKey(keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE ? -1 : keyCode);
            binding = false;
            return;
        }
        settings.forEach(e -> e.keyPressed(keyCode, scanCode, modifiers));
    }
}