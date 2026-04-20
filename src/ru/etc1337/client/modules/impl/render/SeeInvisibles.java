package ru.etc1337.client.modules.impl.render;

import net.minecraft.entity.player.PlayerEntity;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "See Invisibles", description = "Показ невидимых игроков", category = ModuleCategory.RENDER)
public class SeeInvisibles extends Module {
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player && player.isInvisible()) {
                    player.setInvisible(false);
                }
            }
        }
    }
}
