package ru.etc1337.client.modules.impl.misc;

import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Move;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

@ModuleInfo(name = "Anti AFK", description = "Обходит проверку на афк", category = ModuleCategory.MISC)
public class AntiAFK extends Module {
    private final ModeSetting mode = new ModeSetting("Тип", this, "Сообщение", "Прыжки");
    Timer timer = new Timer();

    @Compile
    @Override
    @VMProtect(type = VMProtectType.MUTATION)
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mode.is("Сообщение")) {
                if (!Move.isMoving()) {
                    if (timer.finished(6000)) {
                        mc.player.sendChatMessage("/money");
                        timer.reset();
                    }
                } else {
                    timer.reset();
                }
            } else {
                if (!Move.isMoving()) {
                    if (timer.finished(6000)) {
                        mc.gameSettings.keyBindJump.setPressed(true);
                        timer.reset();
                    } else {
                        mc.gameSettings.keyBindJump.setPressed(false);

                    }
                } else {
                    timer.reset();
                }
            }
        }
    }
}
