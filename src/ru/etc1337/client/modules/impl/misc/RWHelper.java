package ru.etc1337.client.modules.impl.misc;

import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Move;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "RW Helper", description = "PISUN", category = ModuleCategory.MISC)
public class RWHelper extends Module {
    private final MultiModeSetting elements = new MultiModeSetting("Элементы", this, "Dragon Fly");

    @Compile
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.player.abilities.isFlying && elements.get("Dragon Fly").isEnabled()) {
                Move.setMotion(1);
            }
        }
    }
}
