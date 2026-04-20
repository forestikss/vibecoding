package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.notifications.Notification;
import ru.etc1337.api.notifications.NotificationManager;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

@ElementInfo(name = "Notifications", icon = "null", initX = 8.0F, initY = 28.0F, initHeight = 16.0F)
public class NotificationsRenderer extends UIElement {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            MatrixStack matrixStack = eventRender2D.getMatrixStack();
            NotificationManager manager = Client.getInstance().getNotificationManager();

            float y = mc.getMainWindow().getScaledHeight() - 10;


            if (manager.notifications.size() >= 8) {
                manager.notifications.remove(0);
            }

            manager.notifications.removeIf(Notification::shouldDelete);
            manager.notifications.stream()
                    .filter(Notification::isFinished)
                    .forEach(Notification::markForRemoval);

            for (Notification notification : manager.notifications) {
                float offsetY = 16F;
                y = y + offsetY;
                notification.render(matrixStack, getDraggable(), true, y, 0);
            }
        }
    }
}