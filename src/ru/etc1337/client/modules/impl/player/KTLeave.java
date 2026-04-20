package ru.etc1337.client.modules.impl.player;

import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "KT Leave",description = "Позволяет выйти прямо в режиме PVP от противника",category = ModuleCategory.PLAYER)
public class KTLeave extends Module {
    private final ModeSetting mode = new ModeSetting("Мод",this, "Удар себя", "Неверный ID Хотбара");

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mode.is("Удар себя")) {
                if (mc.playerController != null) {
                    mc.playerController.attackEntity(mc.player, mc.player);
                }
            }
            if (mode.is("Неверный ID Хотбара")) {
                mc.player.inventory.currentItem = 1337;
            }
            toggle();
        }
    }
}
