package ru.etc1337.api.render.ui.mainmenu;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.StringTextComponent;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.ColorAnimation;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.interfaces.IScreen;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Glow;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.shaders.impl.Outline;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.render.ui.dropui.DropUi;

import java.awt.*;
import java.net.URI;
import java.util.List;

public class MainMenu extends Screen implements QuickImports {
    private final List<Button> buttons = Lists.newArrayList();

    public MainMenu() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        buttons.forEach(btn -> btn.resize(minecraft, width, height));
    }

    @Override
    protected void init() {
        super.init();

        float margin = 4;
        float btnW   = 200;
        float btnH   = 20F;
        float halfW  = (btnW - margin) / 2f;

        buttons.clear();

        // singleplayer — полная ширина
        buttons.add(new Button(
                new Vector2f(width / 2F - btnW / 2F, height / 2F - 24),
                new Vector2f(btnW, btnH), "Singleplayer",
                (button) -> mc.displayGuiScreen(new WorldSelectionScreen(this))));

        // multiplayer — полная ширина
        buttons.add(new Button(
                new Vector2f(width / 2F - btnW / 2F, height / 2F),
                new Vector2f(btnW, btnH), "Multiplayer",
                (button) -> mc.displayGuiScreen(new MultiplayerScreen(this))));

        // alt manager — левая половина
        buttons.add(new Button(
                new Vector2f(width / 2F - btnW / 2F, height / 2F + 24),
                new Vector2f(halfW, btnH), "Alt Manager",
                (button) -> mc.displayGuiScreen(Client.getInstance().getAccountGui())));

        // options — правая половина
        buttons.add(new Button(
                new Vector2f(width / 2F - btnW / 2F + halfW + margin, height / 2F + 24),
                new Vector2f(halfW, btnH), "Options...",
                (button) -> mc.displayGuiScreen(new OptionsScreen(this, this.minecraft.gameSettings))));

        // quit — полная ширина, красноватый
        buttons.add(new Button(
                new Vector2f(width / 2F - btnW / 2F, height / 2F + 48),
                new Vector2f(btnW, btnH), "Quit Game",
                (button) -> this.minecraft.shutdown()).extra(true));

        buttons.forEach(Button::init);
    }

    // Добавляем в класс MainMenu новые поля
    private final ColorAnimation iconColorAnimation = new ColorAnimation(150);
    private final ColorAnimation textColorAnimation = new ColorAnimation(150);
    private boolean isLogoHovered = false;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        Render.drawImage(matrixStack,
                new ResourceLocation("minecraft", "dreamcore/images/main_menu/5.jpg"), 0, 0, width, height, Color.WHITE);

        int y = (int) (height / 2f - (float) 34 / 2 - ((8 + 32)));
        float centerX = width / 2.0f;

        float iconHeight = Fonts.DREAMCORE_28.height();
        float textHeight = Fonts.SEMIBOLD_18.height();

        FixColor targetIconColor = isLogoHovered ? TempColor.getClientColor().alpha(225) : FixColor.WHITE.alpha(175);
        FixColor targetTextColor = isLogoHovered ? TempColor.getClientColor().alpha(235) : FixColor.WHITE.alpha(185);

        iconColorAnimation.update(targetIconColor);
        textColorAnimation.update(targetTextColor);

        Fonts.SEMIBOLD_18.drawCenter(matrixStack, "stradix", centerX,
                y + textHeight, textColorAnimation.getColor().getRGB());

        buttons.forEach(btn -> {
            btn.alpha(255);
            btn.render(matrixStack, mouseX, mouseY, partialTicks);
        });
    }

    @SneakyThrows
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        buttons.forEach(btn -> btn.mouseClicked(mouseX, mouseY, button));
        if (button == 0 && isLogoHovered) {
            if (!GraphicsEnvironment.isHeadless() && Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI("Stradix.cc"));
                }
            }
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "Stradix.cc"});
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        buttons.forEach(btn -> btn.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        buttons.forEach(btn -> btn.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        buttons.forEach(btn -> btn.keyReleased(keyCode, scanCode, modifiers));
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        buttons.forEach(btn -> btn.charTyped(codePoint, modifiers));
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        buttons.forEach(Button::onClose);
    }

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    public static final class Button implements IScreen {
        private final Vector2f position;
        private final Vector2f size;
        private final String text;
        private final IPressable action;
        private float alpha = 0;
        @Setter
        private boolean extra = false;
        private final ColorAnimation colorAnimation = new ColorAnimation(150);

        @Override
        public void resize(Minecraft minecraft, int width, int height) {

        }

        @Override
        public void init() {

        }

        @Override
        public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
            boolean isHovered = Hover.isHovered(position.x, position.y, size.x, size.y, mouseX, mouseY);
            boolean isIcon = text.equals("⚙") || text.equals("✕");

            // Ванильные цвета кнопки
            FixColor topColor    = extra ? new FixColor(100, 20, 20)   : new FixColor(70, 70, 70);
            FixColor bottomColor = extra ? new FixColor(140, 30, 30)   : new FixColor(50, 50, 50);
            FixColor hoverTop    = extra ? new FixColor(130, 40, 40)   : new FixColor(100, 100, 100);
            FixColor hoverBottom = extra ? new FixColor(170, 50, 50)   : new FixColor(80, 80, 80);

            FixColor bg1 = isHovered ? hoverTop    : topColor;
            FixColor bg2 = isHovered ? hoverBottom : bottomColor;

            Rect rect = new Rect(position.x, position.y, size.x, size.y);

            // Тень (1px снизу и справа)
            Render.drawRect(matrix, position.x, position.y + 1, size.x, size.y,
                    FixColor.BLACK.alpha(80));

            // Основной фон — вертикальный градиент
            Round.draw(matrix, rect, 2,
                    bg1, bg1, bg2, bg2);

            // Обводка
            FixColor border = isHovered
                    ? new FixColor(160, 160, 255)
                    : new FixColor(40, 40, 40);
            ru.etc1337.api.render.shaders.impl.Outline.draw(matrix, rect, 2, 1, border);

            // Текст
            float fontSize = 8;
            float cx = position.x + size.x / 2F;
            float cy = position.y + size.y / 2F;
            FixColor fontColor = isHovered ? FixColor.WHITE : new FixColor(220, 220, 220);

            Fonts.SEMIBOLD_14.drawCenter(matrix, text, cx, cy - fontSize / 2F, fontColor.getRGB());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (Hover.isHovered(position.x, position.y, size.x, size.y, mouseX, mouseY)) {
                action.onPress(this);
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return false;
        }

        @Override
        public void onClose() {

        }

        public interface IPressable {
            void onPress(Button action);
        }
    }
}