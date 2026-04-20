package ru.etc1337.client.modules.impl.render.ui.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.render.ui.dropui.AnimationMath;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

@ElementInfo(name = "Fps", icon = "k", initX = 8.0F, initY = 28.0F, initHeight = 17.0F)
public class FpsRenderer extends UIElement {
    private float fps = 0;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            fps = AnimationMath.fast(fps, mc.getDebugFPS(), 7);
            int fpsCalc = Math.round(fps);
            MatrixStack matrixStack = eventRender2D.getMatrixStack();
            float x = getDraggable().getX();
            float y = getDraggable().getY();

            String fps = String.valueOf(fpsCalc);
            Header.drawModernHeader(matrixStack, this.getDraggable(), x, y, getIcon(), fps, "FPS");
        }
    }
}