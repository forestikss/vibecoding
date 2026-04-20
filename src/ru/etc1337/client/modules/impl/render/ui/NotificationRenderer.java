package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

import java.util.ArrayList;
import java.util.List;

@ElementInfo(name = "Notification", initX = -1, initY = -1, initHeight = 17.0F)
public class NotificationRenderer extends UIElement {

    private static final long SHOW_DURATION = 2000; // время показа
    private static final long FADE_MS       = 300;  // время fade in/out
    private static final float H            = 17f;
    private static final float PAD_X        = 6f;
    private static final float RADIUS       = 8f;
    private static final float SPACING      = 4f;
    private static final int   MAX_VISIBLE  = 5;

    private static final List<Entry> active = new ArrayList<>();

    public static void push(String moduleName, boolean enabled) {
        active.removeIf(e -> e.moduleName.equals(moduleName));
        if (active.size() >= MAX_VISIBLE) active.remove(0);
        active.add(new Entry(moduleName, enabled));
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventRender2D e)) return;
        MatrixStack matrixStack = e.getMatrixStack();

        active.removeIf(entry -> entry.isDead());
        if (active.isEmpty()) return;

        // Базовая позиция — либо draggable (если перетащили), либо центр экрана
        float baseX = getDraggable().getX();
        float baseY = getDraggable().getY();

        // Если позиция не задана (initX = -1) — ставим по центру
        if (baseX < 0 || baseY < 0) {
            baseX = 0; // будет пересчитано для каждого
            baseY = window.getScaledHeight() / 2f + 14f;
        }

        float totalW = 0;

        for (int i = 0; i < active.size(); i++) {
            Entry entry = active.get(i);
            float alpha = entry.getAlpha();
            if (alpha < 0.01f) continue;

            String icon    = "a";
            String label   = entry.moduleName;
            String status  = entry.enabled ? "enabled" : "disabled";
            FixColor statusColor = entry.enabled ? TempColor.getClientColor() : FixColor.GRAY;

            float iconW   = Fonts.DREAMCORE_16.width(icon);
            float labelW  = Fonts.SEMIBOLD_14.width(label);
            float sepW    = Fonts.SEMIBOLD_14.width("  ");
            float statusW = Fonts.SEMIBOLD_14.width(status);
            totalW = PAD_X + iconW + PAD_X + labelW + sepW + statusW + PAD_X;

            // X: если draggable не перемещён — центрируем
            float cx = (getDraggable().getX() < 0)
                    ? window.getScaledWidth() / 2f - totalW / 2f
                    : getDraggable().getX();

            // Y: каждое уведомление ниже предыдущего
            float cy = baseY + i * (H + SPACING);

            // Slide: появляется снизу вверх (небольшой сдвиг)
            float slide = (1f - alpha) * 6f;
            cy += slide;

            // Фон
            FixColor bg = TempColor.getBackgroundColor().alpha((int)(200 * alpha));
            Round.draw(matrixStack, new Rect(cx, cy, totalW, H), RADIUS, bg);

            // Иконка
            float iconX = cx + PAD_X;
            float iconY = cy + H / 2f - Fonts.DREAMCORE_16.height() / 2f;
            Fonts.DREAMCORE_16.draw(matrixStack, icon, iconX, iconY,
                    TempColor.getClientColor().alpha((int)(255 * alpha)).getRGB());

            // Название
            float textY = cy + H / 2f - Fonts.SEMIBOLD_14.height() / 2f;
            float textX = iconX + iconW + PAD_X;
            Fonts.SEMIBOLD_14.draw(matrixStack, label, textX, textY,
                    TempColor.getFontColor().alpha((int)(255 * alpha)).getRGB());

            // Статус
            Fonts.SEMIBOLD_14.draw(matrixStack, status, textX + labelW + sepW, textY,
                    statusColor.alpha((int)(255 * alpha)).getRGB());
        }

        // Обновляем draggable чтобы можно было перетаскивать в чате
        if (totalW > 0) {
            float cx = (getDraggable().getX() < 0)
                    ? window.getScaledWidth() / 2f - totalW / 2f
                    : getDraggable().getX();
            getDraggable().setWidth(totalW);
            getDraggable().setHeight(H * active.size() + SPACING * (active.size() - 1));
            if (getDraggable().getX() < 0) {
                getDraggable().setX(cx);
                getDraggable().setY(window.getScaledHeight() / 2f + 14f);
            }
        }
    }

    private static class Entry {
        final String moduleName;
        final boolean enabled;
        final long spawnTime;

        Entry(String moduleName, boolean enabled) {
            this.moduleName = moduleName;
            this.enabled    = enabled;
            this.spawnTime  = System.currentTimeMillis();
        }

        float getAlpha() {
            long elapsed = System.currentTimeMillis() - spawnTime;
            long total   = SHOW_DURATION + FADE_MS;

            if (elapsed < FADE_MS) {
                // fade in
                return (float) elapsed / FADE_MS;
            } else if (elapsed < SHOW_DURATION) {
                // полная видимость
                return 1f;
            } else if (elapsed < total) {
                // fade out
                return 1f - (float)(elapsed - SHOW_DURATION) / FADE_MS;
            }
            return 0f;
        }

        boolean isDead() {
            return System.currentTimeMillis() - spawnTime > SHOW_DURATION + FADE_MS;
        }
    }
}
