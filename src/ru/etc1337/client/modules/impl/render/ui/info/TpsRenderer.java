package ru.etc1337.client.modules.impl.render.ui.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Maths;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

@ElementInfo(name = "Tps", icon = "l", initX = 8.0F, initY = 28.0F, initHeight = 17.0F)
public class TpsRenderer extends UIElement {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            MatrixStack matrixStack = eventRender2D.getMatrixStack();
            float x = getDraggable().getX();
            float y = getDraggable().getY();
            String tps = String.valueOf(Maths.round(Client.getInstance().getTpsHandler().getTPS(), 1)).replace(".0", "");

            Header.drawModernHeader(matrixStack, this.getDraggable(), x, y, getIcon(), tps, "TPS");
        }
    }
}