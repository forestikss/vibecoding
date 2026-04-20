package ru.etc1337.client.modules.impl.combat;

import net.minecraft.entity.LivingEntity;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventEntityRayTrace;
import ru.etc1337.api.events.impl.game.EventKeepSprint;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "No Entity Trace", description = "Убирает взаимодействия с ентити", category = ModuleCategory.COMBAT)
public class NoEntityTrace extends Module {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventEntityRayTrace entityRayTrace && entityRayTrace.getEntity() instanceof LivingEntity) {
            entityRayTrace.setCancelled(true);
        }
    }
}
