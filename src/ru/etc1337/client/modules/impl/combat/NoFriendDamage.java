package ru.etc1337.client.modules.impl.combat;

import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CUseEntityPacket;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "No Friend Damage", description = "Убирает дамаг по друзьям", category = ModuleCategory.COMBAT)
public class NoFriendDamage extends Module {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventSendPacket eventSendPacket) {
            if (eventSendPacket.getPacket() instanceof CUseEntityPacket cUseEntityPacket) {
                Entity entity = cUseEntityPacket.getEntityFromWorld(mc.world);
                if (entity instanceof RemoteClientPlayerEntity && Client.getInstance().getFriendManager().isFriend(entity) && cUseEntityPacket.getAction() == CUseEntityPacket.Action.ATTACK) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
