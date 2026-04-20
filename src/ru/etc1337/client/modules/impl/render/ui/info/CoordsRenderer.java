package ru.etc1337.client.modules.impl.render.ui.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

@ElementInfo(name = "Coords", icon = "j", initX = 8.0F, initY = 28.0F, initHeight = 17.0F)
public class CoordsRenderer extends UIElement {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            MatrixStack matrixStack = eventRender2D.getMatrixStack();
            float x = getDraggable().getX();
            float y = getDraggable().getY();

            String coordString = String.format("%d, %d, %d", Math.round(mc.player.getPosX()), Math.round(mc.player.getPosY()), Math.round(mc.player.getPosZ()));

            Header.drawModernHeader(matrixStack, this.getDraggable(), x, y, getIcon(), coordString);
        }
    }
}