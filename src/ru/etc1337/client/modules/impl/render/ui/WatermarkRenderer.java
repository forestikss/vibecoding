package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

import java.awt.*;

@ElementInfo(name = "Watermark", initX = 8.0F, initY = 8.0F, initHeight = 17.0F)
public class WatermarkRenderer extends UIElement {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            MatrixStack matrixStack = eventRender2D.getMatrixStack();

            float startX = getDraggable().getX();
            float startY = getDraggable().getY();
            float horizontalSpacing = 2;

            // вычисляем итоговую ширину
            float secondColumnX = startX + Fonts.DREAMCORE_32.width("a") + horizontalSpacing;
            String profile = "%s [%s]".formatted(Client.getInstance().getUserInfo().getUsername(), Client.getInstance().getUserInfo().getRole());
            float thirdColumnX = secondColumnX + horizontalSpacing + Fonts.DREAMCORE_16.width("b");
            float totalWidth = thirdColumnX - startX + Math.max(Fonts.MEDIUM_12.width(profile), Fonts.MEDIUM_12.width("dreamcore.fun"));
            float totalHeight = getDraggable().getHeight();

            // фон темнее чем у других элементов
            float pad = 3f;
            FixColor darkerBg = new FixColor(TempColor.getBackgroundColor().darker().darker()).alpha(230);
            Round.draw(matrixStack,
                    new Rect(startX - pad, startY - pad, totalWidth + pad * 2, totalHeight + pad * 2),
                    4, 4, 4, 4,
                    darkerBg);

            Fonts.DREAMCORE_32.draw(matrixStack, "a", startX, startY, Color.WHITE.getRGB());
            Fonts.DREAMCORE_16.draw(matrixStack, "b", secondColumnX, startY, Color.WHITE.getRGB());
            Fonts.DREAMCORE_12.draw(matrixStack, "c", secondColumnX, startY + Fonts.DREAMCORE_32.height() - Fonts.DREAMCORE_12.height(), Color.WHITE.getRGB());

            Fonts.MEDIUM_12.draw(matrixStack, profile, thirdColumnX, startY + 0.5f, Color.WHITE.getRGB());
            Fonts.MEDIUM_12.draw(matrixStack, "Stradix.cc", thirdColumnX, startY + Fonts.DREAMCORE_32.height() - Fonts.MEDIUM_12.height() - 1F, Color.WHITE.getRGB());

            getDraggable().setWidth(totalWidth);
        }
    }
}