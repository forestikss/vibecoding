package ru.etc1337.client.modules.impl.player;

import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "No Server Rotation", description = "Не дает серверу крутить камеру", category = ModuleCategory.PLAYER)
public class NoServerRotation extends Module {

    @Compile
    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) return;
        if (event instanceof EventReceivePacket packet) {
            if (packet.getPacket() instanceof SPlayerPositionLookPacket sPlayerPositionLookPacket) {
                sPlayerPositionLookPacket.yaw = mc.player.rotationYaw;
                sPlayerPositionLookPacket.pitch = mc.player.rotationPitch;
            }
        }
    }
}
