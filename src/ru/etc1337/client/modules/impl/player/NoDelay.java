package ru.etc1337.client.modules.impl.player;

import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "No Delay", description = "Убирает задержки", category = ModuleCategory.PLAYER)
public class NoDelay extends Module {
    public final MultiModeSetting remove = new MultiModeSetting("Remove", this, "Jump Delay", "Place Delay", "Block Hit Delay");

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (remove.get("Jump Delay").isEnabled()) {
                mc.player.setJumpTicks(0);
            }
            if (remove.get("Place Delay").isEnabled()) {
                mc.setRightClickDelayTimer(0);
            }
            if (remove.get("Block Hit Delay").isEnabled()) {
                mc.playerController.setBlockHitDelay(0);
            }
        }
    }
}