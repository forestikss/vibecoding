package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
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

/**
 * Уведомления о включении/выключении модулей.
 * Появляются снизу вверх, плавно исчезают.
 */
@ElementInfo(name = "ModuleNotify", initX = -1, initY = -1, initHeight = 17.0F)
public class ModuleNotifyRenderer extends UIElement {

    private static final long SHOW_MS  = 2000;
    private static final long FADE_MS  = 300;
    private static final float H       = 18f;
    private static final float PAD_X   = 7f;
    private static final float RADIUS  = 5f;
    private static final float SPACING = 3f;
    private static final int   MAX     = 6;

    private static final List<Entry> entries = new ArrayList<>();

    /** Вызывается из Module.toggle() */
    public static void push(String name, boolean enabled) {
        entries.removeIf(e -> e.name.equals(name));
        if (entries.size() >= MAX) entries.remove(0);
        entries.add(new Entry(name, enabled));
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventRender2D e)) return;
        MatrixStack ms = e.getMatrixStack();

        entries.removeIf(Entry::isDead);
        if (entries.isEmpty()) return;

        // Позиция: правый нижний угол экрана
        float baseX = window.getScaledWidth() - 8f;
        float baseY = window.getScaledHeight() - 8f;

        // Рисуем снизу вверх
        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry entry = entries.get(i);
            float alpha = entry.getAlpha();
            if (alpha < 0.01f) continue;

            // Текст
            String icon    = "a"; // логотип клиента
            String name    = entry.name;
            String status  = entry.enabled ? "on" : "off";
            FixColor statusColor = entry.enabled
                    ? TempColor.getClientColor()
                    : TempColor.getFontColor().alpha(140);

            float iconW   = Fonts.DREAMCORE_12.width(icon);
            float nameW   = Fonts.SEMIBOLD_13.width(name);
            float dotW    = Fonts.SEMIBOLD_13.width("  ");
            float statusW = Fonts.SEMIBOLD_13.width(status);
            float totalW  = PAD_X + iconW + 3f + nameW + dotW + statusW + PAD_X;

            // Slide: появляется справа
            float slide = (1f - alpha) * 12f;

            float rx = baseX - totalW + slide;
            float ry = baseY - H;

            // Фон
            FixColor bg = TempColor.getBackgroundColor().alpha((int)(215 * alpha));
            Round.draw(ms, new Rect(rx, ry, totalW, H), RADIUS, bg);

            // Левая полоска цвета статуса
            FixColor stripe = statusColor.alpha((int)(200 * alpha));
            Round.draw(ms, new Rect(rx, ry, 2f, H),
                    RADIUS, RADIUS, RADIUS, RADIUS, stripe, stripe, stripe, stripe);

            // Иконка
            float iconX = rx + PAD_X;
            float iconY = ry + H / 2f - Fonts.DREAMCORE_12.height() / 2f;
            Fonts.DREAMCORE_12.draw(ms, icon, iconX, iconY,
                    TempColor.getClientColor().alpha((int)(255 * alpha)).getRGB());

            // Название
            float textY = ry + H / 2f - Fonts.SEMIBOLD_13.height() / 2f;
            float textX = iconX + iconW + 3f;
            Fonts.SEMIBOLD_13.draw(ms, name, textX, textY,
                    TempColor.getFontColor().alpha((int)(255 * alpha)).getRGB());

            // Статус
            float statusX = textX + nameW + dotW;
            Fonts.SEMIBOLD_13.draw(ms, status, statusX, textY,
                    statusColor.alpha((int)(255 * alpha)).getRGB());

            baseY -= H + SPACING;
        }

        // Обновляем draggable для перетаскивания
        getDraggable().setWidth(200);
        getDraggable().setHeight(H * entries.size() + SPACING * (entries.size() - 1));
        if (getDraggable().getX() < 0) {
            getDraggable().setX(window.getScaledWidth() - 208f);
            getDraggable().setY(window.getScaledHeight() - 30f);
        }
    }

    private static class Entry {
        final String name;
        final boolean enabled;
        final long spawnTime;

        Entry(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
            this.spawnTime = System.currentTimeMillis();
        }

        float getAlpha() {
            long elapsed = System.currentTimeMillis() - spawnTime;
            long total   = SHOW_MS + FADE_MS;
            if (elapsed < FADE_MS)   return (float) elapsed / FADE_MS;
            if (elapsed < SHOW_MS)   return 1f;
            if (elapsed < total)     return 1f - (float)(elapsed - SHOW_MS) / FADE_MS;
            return 0f;
        }

        boolean isDead() {
            return System.currentTimeMillis() - spawnTime > SHOW_MS + FADE_MS;
        }
    }
}
