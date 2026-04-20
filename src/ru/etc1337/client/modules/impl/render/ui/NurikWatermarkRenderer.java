package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

/**
 * Ватермарка в стиле Nurik:
 * [иконка] clientName  |  [иконка] FPS  |  [иконка] Ping  |  [иконка] BPS  |  [иконка] XYZ
 */
@ElementInfo(name = "NurikWatermark", initX = 8.0F, initY = 8.0F, initHeight = 17.0F)
public class NurikWatermarkRenderer extends UIElement {

    private static final float H       = 17f;
    private static final float PAD     = 5f;
    private static final float SEP     = 1f;   // ширина разделителя
    private static final float RADIUS  = 4f;

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventRender2D e)) return;
        if (mc.player == null && !(mc.currentScreen instanceof ChatScreen)) return;

        MatrixStack ms = e.getMatrixStack();
        float x = getDraggable().getX();
        float y = getDraggable().getY();

        // Данные
        String clientName = Client.clientName;
        String fps   = mc.getDebugFPS() + " FPS";
        String ping  = getPing() + " ms";
        String bps   = String.format("%.1f", Player.getBps(mc.player)) + " BPS";
        String xyz   = mc.player != null
                ? String.format("%d %d %d",
                    (int) mc.player.getPosX(),
                    (int) mc.player.getPosY(),
                    (int) mc.player.getPosZ())
                : "0 0 0";

        // Иконки из DREAMCORE шрифта
        String iconClient = "a"; // логотип
        String iconFps    = "k";
        String iconPing   = "m";
        String iconBps    = "n";
        String iconXyz    = "j";

        // Сегменты: [icon + text]
        Segment[] segments = {
            new Segment(iconClient, clientName, TempColor.getClientColor()),
            new Segment(iconFps,    fps,        TempColor.getFontColor()),
            new Segment(iconPing,   ping,       TempColor.getFontColor()),
            new Segment(iconBps,    bps,        TempColor.getFontColor()),
            new Segment(iconXyz,    xyz,        TempColor.getFontColor()),
        };

        // Считаем общую ширину
        float totalW = PAD;
        for (int i = 0; i < segments.length; i++) {
            totalW += segmentWidth(segments[i]);
            if (i < segments.length - 1) totalW += PAD + SEP + PAD;
        }
        totalW += PAD;

        // Фон
        FixColor bg = TempColor.getBackgroundColor().alpha(210);
        Round.draw(ms, new Rect(x, y, totalW, H), RADIUS, bg);

        // Рисуем сегменты
        float cx = x + PAD;
        for (int i = 0; i < segments.length; i++) {
            Segment seg = segments[i];

            // Иконка
            float iconY = y + H / 2f - Fonts.DREAMCORE_12.height() / 2f;
            Fonts.DREAMCORE_12.draw(ms, seg.icon, cx, iconY,
                    TempColor.getClientColor().getRGB());
            cx += Fonts.DREAMCORE_12.width(seg.icon) + 2f;

            // Текст
            float textY = y + H / 2f - Fonts.SEMIBOLD_13.height() / 2f;
            Fonts.SEMIBOLD_13.draw(ms, seg.text, cx, textY, seg.color.getRGB());
            cx += Fonts.SEMIBOLD_13.width(seg.text);

            // Разделитель
            if (i < segments.length - 1) {
                cx += PAD;
                FixColor sepColor = TempColor.getFontColor().alpha(60);
                ru.etc1337.api.render.Render.drawRect(ms, cx, y + 3, SEP, H - 6, sepColor);
                cx += SEP + PAD;
            }
        }

        getDraggable().setWidth(totalW);
        getDraggable().setHeight(H);
    }

    private float segmentWidth(Segment seg) {
        return Fonts.DREAMCORE_12.width(seg.icon) + 2f + Fonts.SEMIBOLD_13.width(seg.text);
    }

    private int getPing() {
        if (mc.player == null || mc.player.connection == null) return 0;
        var info = mc.player.connection.getPlayerInfo(mc.player.getUniqueID());
        return info != null ? info.getResponseTime() : 0;
    }

    private static class Segment {
        final String icon, text;
        final FixColor color;
        Segment(String icon, String text, FixColor color) {
            this.icon = icon; this.text = text; this.color = color;
        }
    }
}
