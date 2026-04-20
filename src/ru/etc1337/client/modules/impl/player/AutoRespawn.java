package ru.etc1337.client.modules.impl.player;

import net.minecraft.client.gui.screen.DeathScreen;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "Auto Respawn", description = "Автоматически респавнит", category = ModuleCategory.PLAYER)
public class AutoRespawn extends Module {

    @Compile
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.currentScreen instanceof DeathScreen && mc.player.deathTime > 2) {
                mc.player.respawnPlayer();
                mc.displayGuiScreen(null);
            }
        }
    }
}
