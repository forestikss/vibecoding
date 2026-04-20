package ru.etc1337.client.modules.impl.movement;

import net.minecraft.item.Items;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Elytra Bounce", description = "Подлет на элитре", category = ModuleCategory.MOVEMENT)
public class ElytraBounce extends Module {
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.player.inventory.getStackInSlot(38).getItem() == Items.ELYTRA && mc.player.movementInput.jump) {
                mc.player.movementInput.jump = false;
            }
        }
    }
}
