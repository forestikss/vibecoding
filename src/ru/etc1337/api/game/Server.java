package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import ru.etc1337.api.interfaces.QuickImports;

@UtilityClass
public class Server implements QuickImports {
    public int ping() {
        return mc.player.connection.getPlayerInfo(mc.player.getUniqueID()) != null && !is("singleplayer") ?
                mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).getResponseTime() : 0;
    }

    public String getServer() {
        if (mc.world == null || mc.player == null) return "singleplayer";
        String server;
        if (mc.isSingleplayer()) {
            server = "singleplayer";
        } else {
            server = mc.getCurrentServerData() == null ? "singleplayer" : mc.getCurrentServerData().serverIP;
        }
        return server;
    }
    public boolean is(String ip) {
        return getServer().toLowerCase().contains(ip);
    }
    public boolean isRW() {
        return Server.is("reallyworld") || Server.is("playrw");
    }
}
