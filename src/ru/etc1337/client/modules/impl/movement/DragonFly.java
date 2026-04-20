package ru.etc1337.client.modules.impl.movement;

import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Move;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Dragon Fly", description = "Позволяет быстро летать", category = ModuleCategory.MOVEMENT)
public class DragonFly extends Module {
    private final SliderSetting xzSpeed = new SliderSetting("Скорость по X/Z", this, 1, 0, 2, 0.1F);
    private final SliderSetting ySpeed = new SliderSetting("Скорость по Y", this, 1, 0, 2, 0.1F);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.player.abilities.isFlying) {
                Move.setMotion(xzSpeed.getValue());
                mc.player.getMotion().y = 0.0;
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.getMotion().y = 0.25;
                    if (mc.player.moveForward == 0.0f && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown()) {
                        mc.player.getMotion().y = ySpeed.getValue();
                    }
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.getMotion().y = -0.25;
                    if (mc.player.moveForward == 0.0f && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown()) {
                        mc.player.getMotion().y = -ySpeed.getValue();
                    }
                }
            }
        }
    }
}
