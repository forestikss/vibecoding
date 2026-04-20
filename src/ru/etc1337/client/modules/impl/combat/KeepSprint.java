package ru.etc1337.client.modules.impl.combat;

import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventKeepSprint;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "Keep Sprint", description = "Сохраняет спринт при ударе", category = ModuleCategory.COMBAT)
public class KeepSprint extends Module {

    @Compile
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventKeepSprint eventKeepSprint) {
            eventKeepSprint.setCancelled(true);
        }
    }
}
