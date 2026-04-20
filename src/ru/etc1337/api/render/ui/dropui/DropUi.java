package ru.etc1337.api.render.ui.dropui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.render.ui.dropui.elements.ElementModule;
import ru.etc1337.client.modules.api.ModuleCategory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DropUi extends Screen implements QuickImports {

    private static final float BAR_WIDTH   = 100;
    private static final float BAR_HEIGHT  = 16;

    private final List<UIPanel> panels = new ArrayList<>();
    private final FixColor cachedBackgroundColor = FixColor.BLACK.alpha(150);

    private String searchQuery = "";
    private boolean searchFocused = false;

    // Y позиция панелей — вычисляется при init
    private float panelsY = 0;

    public DropUi() {
        super(new TranslationTextComponent("GUI"));
    }

    @Override
    protected void init() {
        super.init();
        initializePanels();
    }

    private void initializePanels() {
        panels.clear();
        int panelWidth = 100;
        int spacing    = 15;
        int totalWidth = ModuleCategory.values().length * (panelWidth + spacing) - spacing;
        int startX     = (width - totalWidth) / 2;
        panelsY        = (height - 200) / 2f;

        for (ModuleCategory category : ModuleCategory.values()) {
            panels.add(new UIPanel(category, startX, (int) panelsY, panelWidth));
            startX += panelWidth + spacing;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Vector2f mouse = new Vector2f(mouseX, mouseY);
        matrixStack.push();

        if (mc.player != null && mc.world != null) {
            Render.drawRect(matrixStack, 0, 0, width, height, cachedBackgroundColor);
        } else {
            Render.drawImage(matrixStack,
                    new ResourceLocation("minecraft", "dreamcore/images/main_menu/5.jpg"),
                    0, 0, width, height, Color.WHITE);
        }

        panels.forEach(panel -> panel.setSearchQuery(searchQuery));
        panels.forEach(panel -> panel.draw(matrixStack, mouse.x, mouse.y));

        // описание только если поиск пустой (фикс бага с зависшим тултипом)
        if (searchQuery.isEmpty()) {
            drawModuleDescriptions(matrixStack, mouse);
        }

        drawSearchBar(matrixStack);
        matrixStack.pop();
    }

    private void drawSearchBar(MatrixStack matrixStack) {
        float barX = width / 2f - BAR_WIDTH / 2f;
        // чуть ниже панелей: panelsY + 18(header) + 200(body) + 8(отступ)
        float barY = panelsY + 18 + 200 + 8;

        FixColor bg = TempColor.getBackgroundColor().alpha(220);
        Round.draw(matrixStack, new Rect(barX, barY, BAR_WIDTH, BAR_HEIGHT), 4, 4, 4, 4, bg, bg, bg, bg);

        String display = searchQuery.isEmpty() && !searchFocused
                ? "search..."
                : searchQuery + (searchFocused ? "|" : "");
        FixColor textColor = searchQuery.isEmpty() && !searchFocused
                ? TempColor.getFontColor().alpha(100)
                : TempColor.getFontColor();

        // центрируем текст внутри бара
        float textW = Fonts.SEMIBOLD_14.width(display);
        float textX = barX + BAR_WIDTH / 2f - textW / 2f;
        float textY = barY + BAR_HEIGHT / 2f - Fonts.SEMIBOLD_14.height() / 2f;
        Fonts.SEMIBOLD_14.draw(matrixStack, display, textX, textY, textColor.getRGB());

        if (searchFocused) {
            Render.drawRect(matrixStack, barX + 2, barY + BAR_HEIGHT - 2, BAR_WIDTH - 4, 1,
                    TempColor.getClientColor().alpha(200));
        }
    }

    private void drawModuleDescriptions(MatrixStack matrixStack, Vector2f mouse) {
        for (UIPanel panel : panels) {
            for (ElementModule element : panel.getElements()) {
                if (element.isHoveringModule) {
                    String desc = element.getModule().getDescription();
                    float tooltipWidth  = Fonts.SEMIBOLD_14.width(desc) + 3;
                    float tooltipHeight = Fonts.SEMIBOLD_14.height() + 1.5f;
                    Render.drawRect(matrixStack, mouse.x + 5, mouse.y - 2, tooltipWidth, tooltipHeight,
                            TempColor.getBackgroundColor().alpha(150));
                    Fonts.SEMIBOLD_14.draw(matrixStack, desc, mouse.x + 7, mouse.y - 3,
                            TempColor.getFontColor().getRGB());
                    return;
                }
            }
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        initializePanels();
        super.resize(minecraft, width, height);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !searchQuery.isEmpty()) {
            searchQuery = "";
            searchFocused = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && searchFocused) {
            if (!searchQuery.isEmpty())
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return true;
        }
        panels.forEach(panel -> panel.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchFocused) {
            // запрет кириллицы
            if (Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.CYRILLIC) return true;
            searchQuery += codePoint;
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        float barX = width / 2f - BAR_WIDTH / 2f;
        float barY = panelsY + 18 + 200 + 8;

        if (mouseX >= barX && mouseX <= barX + BAR_WIDTH
                && mouseY >= barY && mouseY <= barY + BAR_HEIGHT) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        panels.forEach(panel -> panel.mouseClicked((float) mouseX, (float) mouseY, mouseButton));
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        panels.forEach(panel -> panel.mouseScrolled((float) mouseX, (float) mouseY, (float) delta));
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        panels.forEach(panel -> panel.mouseReleased((float) mouseX, (float) mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
