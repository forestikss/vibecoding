package ru.etc1337.client.modules.impl.player;

import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.Arrays;

@ModuleInfo(name = "Auto Accept", description = "Автоматически принимает телепортацию", category = ModuleCategory.PLAYER)
public class AutoAccept extends Module {
    private final BooleanSetting onlyfriends = new BooleanSetting("Только друзей", this);
    private final String[] teleportMessages = new String[]{"has requested teleport", "просит телепортироваться", "просит к вам телепортироваться", "хочет телепортироваться к вам"};

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventReceivePacket eventReceivePacket) {
            if (eventReceivePacket.getPacket() instanceof SChatPacket) {
                SChatPacket packetChat = (SChatPacket) eventReceivePacket.getPacket();
                handleReceivePacket(packetChat);
            }
        }
    }

    private void handleReceivePacket(SChatPacket packet) {
        String message = TextFormatting.getTextWithoutFormattingCodes(packet.getChatComponent().getString());

        if (isTeleportMessage(message)) {
            if (onlyfriends.isEnabled()) {
                handleTeleportWithFriends(message);
                return;
            }

            mc.player.sendChatMessage("/tpaccept");
        }
    }

    private boolean isTeleportMessage(String message) {
        return Arrays.stream(this.teleportMessages).map(String::toLowerCase).anyMatch(message::contains);
    }

    private void handleTeleportWithFriends(String message) {
        for (String friend : Client.getInstance().getFriendManager().getFriends()) {
            StringBuilder builder = new StringBuilder();
            char[] buffer = message.toCharArray();
            for (int w = 0; w < buffer.length; w++) {
                char c = buffer[w];
                if (c == '§') {
                    w++;
                } else {
                    builder.append(c);
                }
            }

            if (builder.toString().contains(friend)) {
                mc.player.sendChatMessage("/tpaccept");
            }
        }
    }
}
