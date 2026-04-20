package ru.etc1337.client.modules.impl.misc;

import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "RP Spoofer", description = "Отключает ресурс пак сервера", category = ModuleCategory.MISC)
public class RPSpoofer extends Module {
    @Compile
    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) return;
        if (event instanceof EventReceivePacket eventSendPacket) {
            if (eventSendPacket.getPacket() instanceof SSendResourcePackPacket) {
                mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
                mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
                eventSendPacket.setCancelled(true);
            }
        }
    }
}
