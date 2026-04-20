package ru.etc1337.api.bot;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;

/**
 * Экран управления ботом.
 * WASD — движение, Space — прыжок, Shift — присесть, Sprint — Ctrl
 * ЛКМ — атака, ПКМ — использовать предмет
 * T — открыть чат бота, Esc — выйти из управления
 */
public class BotControlScreen extends Screen implements QuickImports {

    private final BotConnection bot;

    // Состояние клавиш
    private boolean w, a, s, d, space, shift, ctrl;

    // Чат
    private boolean chatOpen = false;
    private String chatInput = "";

    public BotControlScreen(BotConnection bot) {
        super(StringTextComponent.EMPTY);
        this.bot = bot;
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        super.render(ms, mouseX, mouseY, partialTicks);

        // Полупрозрачный оверлей
        Render.drawRect(ms, 0, 0, width, height, FixColor.BLACK.alpha(80));

        // Статус
        FixColor col = bot.isConnected() ? TempColor.getClientColor() : FixColor.RED;
        String status = bot.isConnected() ? "Online" : "Offline";
        Fonts.SEMIBOLD_14.draw(ms, "Bot: §f" + bot.getName() + " §7| " + status,
                5, 5, col.getRGB());
        Fonts.SEMIBOLD_14.draw(ms, "§7WASD — движение  Space — прыжок  Shift — присесть  Ctrl — спринт",
                5, 18, TempColor.getFontColor().alpha(180).getRGB());
        Fonts.SEMIBOLD_14.draw(ms, "§7T — чат  Esc — выйти из управления",
                5, 30, TempColor.getFontColor().alpha(180).getRGB());

        // Индикаторы клавиш
        drawKey(ms, "W", width / 2f - 12, height / 2f - 30, w);
        drawKey(ms, "A", width / 2f - 30, height / 2f - 12, a);
        drawKey(ms, "S", width / 2f - 12, height / 2f - 12, s);
        drawKey(ms, "D", width / 2f + 6,  height / 2f - 12, d);
        drawKey(ms, "↑", width / 2f - 12, height / 2f + 6, space);
        drawKey(ms, "⇓", width / 2f + 24, height / 2f + 6, shift);
        drawKey(ms, ">>", width / 2f + 42, height / 2f + 6, ctrl);

        // Чат-инпут
        if (chatOpen) {
            float bx = width / 2f - 100, by = height - 30;
            Round.draw(ms, new Rect(bx, by, 200, 16), 4, TempColor.getBackgroundColor().alpha(200));
            Fonts.SEMIBOLD_14.draw(ms, chatInput + "|", bx + 4, by + 2,
                    TempColor.getFontColor().getRGB());
        }

        // Последние сообщения чата бота
        int cy = height - 60;
        String[] msgs = bot.getChatMessages().toArray(new String[0]);
        int start = Math.max(0, msgs.length - 5);
        for (int i = start; i < msgs.length; i++) {
            Fonts.SEMIBOLD_13.draw(ms, msgs[i], 5, cy, TempColor.getFontColor().alpha(200).getRGB());
            cy -= 12;
        }
    }

    private void drawKey(MatrixStack ms, String label, float x, float y, boolean pressed) {
        FixColor bg = pressed ? TempColor.getClientColor().alpha(180) : TempColor.getBackgroundColor().alpha(150);
        Round.draw(ms, new Rect(x, y, 20, 16), 3, bg);
        float tw = Fonts.SEMIBOLD_13.width(label);
        Fonts.SEMIBOLD_13.draw(ms, label, x + 10 - tw / 2, y + 2, TempColor.getFontColor().getRGB());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (chatOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { chatOpen = false; chatInput = ""; return true; }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (!chatInput.isEmpty()) bot.sendChat(chatInput);
                chatInput = ""; chatOpen = false; return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !chatInput.isEmpty()) {
                chatInput = chatInput.substring(0, chatInput.length() - 1); return true;
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            bot.setControlled(false);
            mc.displayGuiScreen(null);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_T) { chatOpen = true; return true; }

        updateMovement(keyCode, true);
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        updateMovement(keyCode, false);
        return true;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (chatOpen) { chatInput += c; return true; }
        return false;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (bot.getPlayHandler() == null) return false;
        if (button == 0) bot.getPlayHandler().attack();
        if (button == 1) bot.getPlayHandler().useItem();
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        // Поворот камеры бота по движению мыши
        if (bot.getPlayHandler() != null) {
            float yaw   = (float)(mx / width  * 360f - 180f);
            float pitch = (float)(my / height * 180f - 90f);
            bot.getPlayHandler().setRotation(yaw, pitch);
        }
        return true;
    }

    private void updateMovement(int keyCode, boolean pressed) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_W -> w = pressed;
            case GLFW.GLFW_KEY_A -> a = pressed;
            case GLFW.GLFW_KEY_S -> s = pressed;
            case GLFW.GLFW_KEY_D -> d = pressed;
            case GLFW.GLFW_KEY_SPACE -> space = pressed;
            case GLFW.GLFW_KEY_LEFT_SHIFT -> shift = pressed;
            case GLFW.GLFW_KEY_LEFT_CONTROL -> ctrl = pressed;
        }
        if (bot.getPlayHandler() != null) {
            bot.getPlayHandler().setMovement(w, s, a, d, space, shift, ctrl);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
