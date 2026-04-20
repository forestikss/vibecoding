package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SEntityStatusPacket;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.game.EventTotemPop;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.interfaces.QuickImports;

import java.util.HashMap;


public class PlayerInfoLogger implements EventListener, QuickImports {
    public HashMap<String, Integer> popList = new HashMap<>();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventReceivePacket eventReceivePacket) {
            if (eventReceivePacket.getPacket() instanceof SEntityStatusPacket pac) {
                if (pac.getOpCode() == 35) { // totem_break
                    Entity ent = pac.getEntity(mc.world);
                    if (!(ent instanceof PlayerEntity player)) return;
                    if (popList == null) {
                        popList = new HashMap<>();
                    }
                    if (popList.get(ent.getName().getString()) == null) {
                        popList.put(ent.getName().getString(), 1);
                    } else if (popList.get(ent.getName().getString()) != null) {
                        popList.put(ent.getName().getString(), popList.get(ent.getName().getString()) + 1);
                    }

                    player.setTotemPops(player.getTotemPops() + 1);
                    new EventTotemPop(player, popList.get(ent.getName().getString())).hook();
                }
            }
        }
        if (event instanceof EventTick eventTick) {
            if (mc.player == null || mc.world == null) return;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player.getRealHealth() <= 0 && popList.containsKey(player.getName().getString())) {
                    popList.remove(player.getName().getString(), popList.get(player.getName().getString()));
                }
            }
        }
    }

    public int getPops(PlayerEntity entity) {
        if (popList.get(entity.getName().getString()) == null) return 0;
        return popList.get(entity.getName().getString());
    }
}
