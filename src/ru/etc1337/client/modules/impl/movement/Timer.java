package ru.etc1337.client.modules.impl.movement;

import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Timer", category = ModuleCategory.MOVEMENT)
public class Timer extends Module {

    private final SliderSetting timerValue = new SliderSetting("Value", this, 1, 1, 5, 0.1f);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            mc.getTimer().timerSpeed = timerValue.getValue();
        }
    }
    @Override
    public void onDisable() {
        mc.getTimer().timerSpeed = 1.0f;
        super.onDisable();
    }
}
