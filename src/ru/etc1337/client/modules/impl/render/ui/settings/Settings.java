package ru.etc1337.client.modules.impl.render.ui.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.settings.impl.*;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

@Getter
@ElementInfo(name = "Settings", icon = "s", initX = 8.0F, initY = 28.0F, initHeight = 17.0F)
public class Settings extends UIElement {
    public final ColorSetting clientColor = new ColorSetting("Цвет клиента", this,
            new FixColor(255, 255, 255,255).getRGB());

    public final ColorSetting backgroundColor = new ColorSetting("Цвет заднего фона", this,
            new FixColor(30, 32, 40, 255).getRGB());

    public final ColorSetting fontColor = new ColorSetting("Цвет шрифта", this,
            new FixColor(255, 255, 255,255).getRGB());

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventTick) {
            TempColor.setCustomAccentColor(clientColor.getColor());

            TempColor.setCustomBackgroundColor(backgroundColor.getColor());

            TempColor.setCustomTextColor(fontColor.getColor());
        }

        if (event instanceof EventRender2D eventRender2D) {
            if (!(mc.currentScreen instanceof ChatScreen)) return;
            MatrixStack matrixStack = eventRender2D.getMatrixStack();
            float x = getDraggable().getX();
            float y = getDraggable().getY();

            Header.drawModernHeader(matrixStack, getDraggable(), x, y, getIcon(), "Нажми ПКМ чтобы открыть настройки");
        }
    }
}