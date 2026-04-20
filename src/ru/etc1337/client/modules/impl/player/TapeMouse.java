package ru.etc1337.client.modules.impl.player;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Tape Mouse", description = "Нажимает на кнопку мыши по времени", category = ModuleCategory.PLAYER)
public class TapeMouse extends Module {
    private ModeSetting mode = new ModeSetting("Кнопка мыши", this, "Левая");
    private SliderSetting delay = new SliderSetting("Задержка", this, 1000, 100, 10000, 50);
    Timer timer = new Timer();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (timer.finished((long) delay.getValue())) {
                if (mode.is("Левая")) {
                    if (/*mc.pointedEntity != null && !mc.pointedEntity.isAlive() || */mc.pointedEntity != null && Client.getInstance().getFriendManager().isFriend(mc.pointedEntity)) return;
                    mc.gameSettings.pauseOnLostFocus = false;
                    mc.currentScreen = null;
                    mc.clickMouse();
                }
                timer.reset();
            }
        }
    }
}
