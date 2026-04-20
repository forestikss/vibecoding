package ru.etc1337.client.modules.impl.misc;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventRemoveEntity;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Leave Tracker", description = "Отслеживает координаты игрока", category = ModuleCategory.MISC)
public class LeaveTracker extends Module {
    private BooleanSetting checkDistance = new BooleanSetting("Проверять дистанцию", this);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRemoveEntity eventRemoveEntity) {
            if (eventRemoveEntity.getEntity() instanceof AbstractClientPlayerEntity clientPlayer) {
                if (clientPlayer instanceof ClientPlayerEntity || checkDistance.isEnabled() && mc.player.getDistance(clientPlayer) < 100) {
                    return;
                }
                String name = clientPlayer.getNameClear();
                if (name.contains("CIT-")) return;

                String x = String.format("%.1f", clientPlayer.getPosX());
                String y = String.format("%.1f", clientPlayer.getPosY());
                String z = String.format("%.1f", clientPlayer.getPosZ());

                Chat.send(String.format("Игрок %s телепортировался на координаты x: %s y: %s z: %s", clientPlayer.getNameClear(), x, y, z));
            }
        }
    }
}
