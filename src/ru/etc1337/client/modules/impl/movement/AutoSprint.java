package ru.etc1337.client.modules.impl.movement;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.KillAura;

@Setter @Getter
@ModuleInfo(name = "Auto Sprint", description = "Автоматический бег", category = ModuleCategory.MOVEMENT)
public class AutoSprint extends Module {
    private boolean canSprint = true;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate eventUpdate) {
            KillAura killAura = Client.getInstance().getHit().getKillAura();
            if (!killAura.legitSprint.isEnabled() && !canSprint && Inventory.Use.script.isFinished()) canSprint = true;
        }
    }
}
