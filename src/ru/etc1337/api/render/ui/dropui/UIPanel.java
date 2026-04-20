package ru.etc1337.api.render.ui.dropui;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.render.ui.dropui.elements.Element;
import ru.etc1337.api.render.ui.dropui.elements.ElementModule;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;

import java.util.ArrayList;
import java.util.List;

public class UIPanel {
    private static final float headerHeight = 18.0f;

    private final ModuleCategory category;
    private final float x;
    private final float y;
    private final float width;
    private final float height = 200;
    @Getter
    private final List<ElementModule> elements = new ArrayList<>();
    private float scrollOffset = 0;
    private float scrollVelocity = 0;
    private float maxScrollOffset = 0;

    public UIPanel(ModuleCategory category, float x, float y, float width) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        for (Module module : Client.getInstance().getModuleManager().getModules()) {
            if (module.getCategory() == category) {
                elements.add(new ElementModule(module));
            }
        }
        updateMaxScrollOffset();
    }

    public void draw(MatrixStack matrixStack, float mouseX, float mouseY) {
        scrollOffset = MathHelper.clamp(scrollOffset + scrollVelocity, 0, maxScrollOffset);
        scrollVelocity *= 0.9f;
        renderPanel(matrixStack);
        renderElements(matrixStack, mouseX, mouseY);
    }

    private void renderPanel(MatrixStack matrixStack) {
        FixColor backgroundColor = TempColor.getBackgroundColor();
        float round = 8;

        Round.draw(matrixStack, new Rect(x - 0.5f, y - 0.5f, width + 1, headerHeight - 1.5f),
                round, round, 0, 0, backgroundColor, backgroundColor, backgroundColor, backgroundColor);

        Round.draw(matrixStack, new Rect(x - 0.5f, y + headerHeight - 2, width + 1, height + 3),
                0, 0, round, round, backgroundColor.alpha(225), backgroundColor.alpha(225),
                backgroundColor.alpha(225), backgroundColor.alpha(225));

        float nameWidth = Fonts.SEMIBOLD_16.width(category.getDisplayName()) / 2.0f;
        float centerX = x + width / 2.0f;
        Fonts.DREAMCORE_16.drawCenter(matrixStack, category.getIcon(), centerX - nameWidth - 7,
                y + Fonts.DREAMCORE_16.height() / 2.0F, TempColor.getClientColor().getRGB());
        Fonts.SEMIBOLD_16.drawCenter(matrixStack, category.getDisplayName(), centerX,
                y + Fonts.SEMIBOLD_16.height() / 2.0f, TempColor.getFontColor().getRGB());
    }

    private void renderElements(MatrixStack matrixStack, float mouseX, float mouseY) {
        Render.startScissor(x, y + headerHeight, width, height);
        float offset = -scrollOffset;
        for (ElementModule element : elements) {
            if (!element.isVisible()) continue;
            float elementY = y + headerHeight + offset;
            float totalHeight = calculateElementHeight(element);

            if (elementY < y + headerHeight + height && elementY + totalHeight > y + headerHeight) {
                element.x = x;
                element.y = elementY;
                element.width = width;
                element.draw(matrixStack, mouseX, mouseY);
            }
            offset += totalHeight;
        }
        Render.endScissor();
    }

    private float calculateElementHeight(ElementModule element) {
        float elementHeight = element.getHeight();
        if (element.extended) {
            for (Element subElement : element.settings) {
                elementHeight += subElement.getHeight();
            }
        }
        return elementHeight;
    }

    private void updateMaxScrollOffset() {
        float totalHeight = 0;
        for (ElementModule element : elements) {
            totalHeight += calculateElementHeight(element);
        }
        maxScrollOffset = Math.max(0, totalHeight - height);
    }

    public void mouseScrolled(float mouseX, float mouseY, float delta) {
        if (isHovered(mouseX, mouseY)) {
            scrollVelocity -= delta * 2.0f;
            updateMaxScrollOffset();
        }
    }

    public void mouseClicked(float mouseX, float mouseY, int mouseButton) {
        if (!isHovered(mouseX, mouseY)) {
            return;
        }

        float offset = -scrollOffset;
        for (ElementModule element : elements) {
            if (!element.isVisible()) continue;
            float elementY = y + headerHeight + offset;
            float totalHeight = calculateElementHeight(element);

            if (elementY < y + headerHeight + height && elementY + totalHeight > y + headerHeight) {
                element.x = x;
                element.y = elementY;
                element.width = width;
                element.mouseClicked(mouseX, mouseY, mouseButton);
            }
            offset += totalHeight;
        }
    }

    public void mouseReleased(float mouseX, float mouseY, int state) {
        elements.forEach(e -> e.mouseReleased(mouseX, mouseY, state));
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        elements.forEach(e -> e.keyPressed(keyCode, scanCode, modifiers));
    }

    private boolean isHovered(float mouseX, float mouseY) {
        return Hover.isHovered(x, y + headerHeight, width, height, mouseX, mouseY);
    }

    public void setSearchQuery(String query) {
        for (ElementModule element : elements) {
            boolean visible = query.isEmpty() ||
                    element.getModule().getName().toLowerCase().contains(query.toLowerCase());
            element.setVisible(visible);
        }
        updateMaxScrollOffset();
    }

    public static float map(float value, float inputStart, float inputEnd, float outputStart, float outputEnd) {
        return outputStart + (outputEnd - outputStart) * ((value - inputStart) / (inputEnd - inputStart));
    }
}