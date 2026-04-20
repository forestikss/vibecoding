package ru.etc1337.api.render.ui.mainmenu.account;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.ColorAnimation;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.interfaces.IScreen;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.Stencil;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Glow;
import ru.etc1337.api.render.shaders.impl.Round;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class AccountGuiScreen extends Screen implements QuickImports {
    private TextBox textBox;
    private boolean exitScreen = false;
    private final ScrollUtil scroll = new ScrollUtil();
    @Setter
    private Account selected = null;
    private final List<Button> buttons = Lists.newArrayList();

    private static final ResourceLocation BACKGROUND_IMAGE = new ResourceLocation("minecraft", "dreamcore/images/main_menu/5.jpg");
    private static final float PANEL_WIDTH = 280;
    private static final float PANEL_HEIGHT = 240;
    private static final float MARGIN = 10;
    private static final float APPEND = 5;
    private static final float BUTTON_SIZE = 20;

    private final ColorAnimation titleColorAnimation = new ColorAnimation(150);

    public AccountGuiScreen() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        Client.getInstance().getAccountManager().forEach(account -> account.resize(minecraft, width, height));
        buttons.forEach(btn -> btn.resize(minecraft, width, height));
    }

    @Override
    protected void init() {
        super.init();
        exitScreen = false;
        buttons.clear();

        // Initialize TextBox
        if (textBox == null) {
            textBox = new TextBox(new Vector2f(0, 0), Fonts.SEMIBOLD_14, 8,
                    TempColor.getClientColor().getRGB(), TextBox.TextAlign.LEFT, "Enter desired name", 0, false, false);
        }
        if (!Client.getInstance().getAccountManager().isAccount(mc.session.getProfile().getName())) {
            textBox.setText(mc.session.getProfile().getName());
        } else {
            textBox.setText("");
        }
        textBox.setCursor(textBox.getText().length());

        // Add current session account if not managed
        if (!Client.getInstance().getAccountManager().isAccount(mc.session.getProfile().getName())) {
            Client.getInstance().getAccountManager().addAccount(new Account(LocalDateTime.now(), mc.session.getProfile().getName()));
        }

        // Select first account if none is selected
        if (selected == null && !Client.getInstance().getAccountManager().isEmpty()) {
            setSelected(Client.getInstance().getAccountManager().stream().findFirst().orElse(null));
        }

        // Initialize buttons
        float panelX = this.width / 2F - PANEL_WIDTH / 2F;
        float bottomPanelY = this.height / 2F - PANEL_HEIGHT / 2F + PANEL_HEIGHT + MARGIN;
        buttons.add(new Button(new Vector2f(panelX + PANEL_WIDTH - BUTTON_SIZE, bottomPanelY),
                new Vector2f(BUTTON_SIZE, BUTTON_SIZE), "★",
                (button) -> {
                    if (selected != null) {
                        selected.toggleFavorite();
                        Client.getInstance().getAccountManager().save();
                    }
                }));
        buttons.add(new Button(new Vector2f(panelX + PANEL_WIDTH - BUTTON_SIZE - APPEND - BUTTON_SIZE, bottomPanelY),
                new Vector2f(BUTTON_SIZE, BUTTON_SIZE), "♻",
                (button) -> {
                    String generatedName = "Generated" + UUID.randomUUID().toString().substring(0, 8);
                    minecraft.session = new Session(generatedName, "", "", "");
                    textBox.setText(minecraft.session.getProfile().getName());
                    textBox.setCursor(minecraft.session.getProfile().getName().length());
                    Account account = new Account(LocalDateTime.now(), minecraft.session.getProfile().getName());
                    Client.getInstance().getAccountManager().addAccount(account);
                    selected = account;
                    scroll.setTarget(0);
                    scroll.update();
                }));

        Client.getInstance().getAccountManager().forEach(Account::init);
        buttons.forEach(Button::init);
        scroll.reset();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.closeCheck();

        Render.drawImage(matrixStack, BACKGROUND_IMAGE, 0, 0, width, height, Color.WHITE);

        float panelX = this.width / 2F - PANEL_WIDTH / 2F;
        float panelY = this.height / 2F - PANEL_HEIGHT / 2F;

        // Title
        boolean isTitleHovered = Hover.isHovered(panelX + MARGIN, panelY - MARGIN - 8, Fonts.SEMIBOLD_14.width("Account Manager"), 8, mouseX, mouseY);
        FixColor targetTitleColor = isTitleHovered ? TempColor.getClientColor().alpha(235) : FixColor.WHITE.alpha(185);
        titleColorAnimation.update(targetTitleColor);
        Fonts.SEMIBOLD_14.draw(matrixStack, "Account Manager", panelX + MARGIN, panelY - MARGIN - 8, titleColorAnimation.getColor().getRGB());

        // Player Nickname
        String playerName = "Your Nickname: " + mc.session.getProfile().getName();
        Fonts.SEMIBOLD_14.draw(matrixStack, playerName,
                panelX + PANEL_WIDTH - MARGIN - Fonts.SEMIBOLD_14.width(playerName),
                panelY - MARGIN - 8,
                titleColorAnimation.getColor().getRGB());

        // Tips
        List<String> tips = new ArrayList<>();
        tips.add("Left click - Use account");
        tips.add("Right click - Delete account");
        float tipY = this.height - MARGIN - 6;
        for (String tip : tips) {
            Fonts.SEMIBOLD_14.draw(matrixStack, tip,
                    this.width / 2F - Fonts.SEMIBOLD_14.width(tip) / 2F,
                    tipY,
                    TempColor.getFontColor().alpha(200).getRGB());
            tipY -= 8;
        }

        // Main panel
        Round.draw(matrixStack, new Rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT), 2, TempColor.getBackgroundColor().alpha(155));
        Glow.draw(matrixStack, new Rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT), 8.0F, 1.0F, 6.0F,
                TempColor.getBackgroundColor().alpha(155),
                TempColor.getBackgroundColor(),
                TempColor.getBackgroundColor(),
                TempColor.getBackgroundColor().alpha(155));


        scroll.update();

        float currentOffset = 0;

        Stencil.init();
        Render.drawRect(matrixStack, panelX, panelY + MARGIN / 2F, PANEL_WIDTH, PANEL_HEIGHT - MARGIN, TempColor.getBackgroundColor());
        Stencil.read(1);

        // Render Favorite Accounts
        if (!Client.getInstance().getAccountManager().getFavoriteAccountsSorted().isEmpty()) {
            Fonts.SEMIBOLD_14.draw(matrixStack, "Favorites", panelX + MARGIN, panelY + scroll.getWheel() + MARGIN + currentOffset, TempColor.getFontColor().alpha(200).getRGB());
            currentOffset += 6F + MARGIN;
        }

        for (Account account : Client.getInstance().getAccountManager().getFavoriteAccountsSorted()) {
            account.position().set(panelX + MARGIN, panelY + scroll.getWheel() + MARGIN + currentOffset);
            account.size().set(PANEL_WIDTH - MARGIN * 2, 20);
            account.render(matrixStack, mouseX, mouseY, partialTicks);
            currentOffset += 25;
        }

        // Render Other Accounts
        List<Account> nonFavoriteAccounts = Client.getInstance().getAccountManager().stream()
                .filter(account -> !account.favorite()).toList();
        if (!nonFavoriteAccounts.isEmpty()) {
            if (!Client.getInstance().getAccountManager().getFavoriteAccountsSorted().isEmpty()) {
                currentOffset += MARGIN / 2F;
            }
            Fonts.SEMIBOLD_14.draw(matrixStack, "Account List", panelX + MARGIN, panelY + scroll.getWheel() + MARGIN + currentOffset, TempColor.getFontColor().alpha(200).getRGB());
            currentOffset += 6F + MARGIN;
        }

        for (Account account : nonFavoriteAccounts) {
            account.position().set(panelX + MARGIN, panelY + scroll.getWheel() + MARGIN + currentOffset);
            account.size().set(PANEL_WIDTH - MARGIN * 2, 20);
            account.render(matrixStack, mouseX, mouseY, partialTicks);
            currentOffset += 25;
        }
        Stencil.finish();

        scroll.setMax(currentOffset - (MARGIN / 2F), PANEL_HEIGHT - (MARGIN * 2F));
        scroll.renderV(matrixStack, new Vector2f(panelX + PANEL_WIDTH - 5, panelY + MARGIN), PANEL_HEIGHT - (MARGIN * 2F), 255);

        // Bottom panel with input field
        float bottomPanelY = panelY + PANEL_HEIGHT + MARGIN;
        textBox.position.set(panelX + APPEND, bottomPanelY + APPEND);
        textBox.setWidth(Math.max(100, Fonts.SEMIBOLD_14.width(textBox.isEmpty() ? textBox.getEmptyText() : textBox.getText()) + 10));
        Round.draw(matrixStack, new Rect(panelX, bottomPanelY, textBox.getWidth() + APPEND * 2, BUTTON_SIZE), 2, TempColor.getBackgroundColor().alpha(155));
        if (textBox.isSelected()) {
            Glow.draw(matrixStack, new Rect(panelX, bottomPanelY, textBox.getWidth() + APPEND * 2, BUTTON_SIZE), 8.0F, 1.0F, 6.0F,
                    TempColor.getBackgroundColor().alpha(155),
                    TempColor.getBackgroundColor(),
                    TempColor.getBackgroundColor(),
                    TempColor.getBackgroundColor().alpha(155));
        }
        textBox.draw(matrixStack);

        // Render buttons
        buttons.forEach(btn -> {
            btn.alpha(255);
            btn.render(matrixStack, mouseX, mouseY, partialTicks);
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (exitScreen) return false;

        textBox.mouse(mouseX, mouseY, button);
        buttons.forEach(btn -> btn.mouseClicked(mouseX, mouseY, button));

        if (Hover.isHovered(this.width / 2F - PANEL_WIDTH / 2F, this.height / 2F - PANEL_HEIGHT / 2F + MARGIN / 2F, PANEL_WIDTH, PANEL_HEIGHT - MARGIN, mouseX, mouseY)) {
            for (Account account : Client.getInstance().getAccountManager()) {
                if (account.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Client.getInstance().getAccountManager().forEach(account -> account.mouseReleased(mouseX, mouseY, button));
        buttons.forEach(btn -> btn.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (exitScreen) return false;

        boolean handledByAccount = false;
        for (Account account : Client.getInstance().getAccountManager()) {
            if (account.keyPressed(keyCode, scanCode, modifiers)) {
                handledByAccount = true;
                break;
            }
        }
        if (handledByAccount) return true;

        String prevText = textBox.getText();
        textBox.keyPressed(keyCode);

        if (textBox.isSelected() && keyCode == GLFW.GLFW_KEY_ENTER) {
            textBox.setSelected(false);
            if (!textBox.getText().isEmpty() && textBox.getText().length() >= 3) {
                minecraft.session = new Session(textBox.getText(), "", "", "");
                Account account = new Account(LocalDateTime.now(), textBox.getText());
                Client.getInstance().getAccountManager().addAccount(account);
                selected = account;
                scroll.setTarget(0);
                scroll.update();
                textBox.setCursor(textBox.getText().length());
            }
            return true;
        }

        buttons.forEach(btn -> btn.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        Client.getInstance().getAccountManager().forEach(account -> account.keyReleased(keyCode, scanCode, modifiers));
        buttons.forEach(btn -> btn.keyReleased(keyCode, scanCode, modifiers));
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (exitScreen) return false;

        boolean handledByAccount = false;
        for (Account account : Client.getInstance().getAccountManager()) {
            if (account.charTyped(codePoint, modifiers)) {
                handledByAccount = true;
                break;
            }
        }
        if (handledByAccount) return true;

        if (textBox.getText().length() < 16) {
            String prevText = textBox.getText();
            textBox.charTyped(codePoint);
        }
        buttons.forEach(btn -> btn.charTyped(codePoint, modifiers));
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        this.exitScreen = true;
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        Client.getInstance().getAccountManager().forEach(Account::onClose);
        textBox.setSelected(false);
        Client.getInstance().getAccountManager().save();
        buttons.forEach(Button::onClose);
    }

    private void closeCheck() {
        if (exitScreen) {
            Minecraft.getInstance().displayGuiScreen(null);
            exitScreen = false;
        }
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
            FixColor hoveredColor = text.equals("★") ? TempColor.getClientColor().alpha(255) : TempColor.getBackgroundColor().alpha(255);
            FixColor defaultColor = text.equals("★") ? TempColor.getClientColor().alpha(155) : TempColor.getBackgroundColor().alpha(155);
            FixColor fontColor = isHovered ? TempColor.getFontColor() : TempColor.getFontColor().alpha(200);

            colorAnimation.update(isHovered ? hoveredColor : defaultColor);
            Rect rect = new Rect(position.x, position.y, size.x, size.y);
            Round.draw(matrix, rect, 2, colorAnimation.getColor());
            if (isHovered) {
                Glow.draw(matrix, rect, 8.0F, 1.0F, 6.0F,
                        colorAnimation.getColor().alpha(155),
                        colorAnimation.getColor(),
                        colorAnimation.getColor(),
                        colorAnimation.getColor().alpha(155));
            }

            Fonts.SEMIBOLD_14.drawCenter(matrix, text, position.x + (size.x / 2F), position.y + (size.y / 2F) - 4, fontColor.getRGB());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (Hlp.isLClick(button) && Hover.isHovered(position.x, position.y, size.x, size.y, mouseX, mouseY)) {
                action.onPress(this);
                return true;
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