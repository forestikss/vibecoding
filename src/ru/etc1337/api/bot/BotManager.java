package ru.etc1337.api.bot;

import ru.etc1337.api.interfaces.QuickImports;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class BotManager implements QuickImports {

    private final Map<String, BotConnection> bots = new ConcurrentHashMap<>();

    public BotConnection connect(String name) {
        if (bots.containsKey(name)) {
            bots.get(name).disconnect();
        }

        String host = "localhost";
        int port = 25565;

        // Получаем адрес из текущего подключения игрока
        try {
            if (mc.player != null && mc.player.connection != null) {
                SocketAddress addr = mc.player.connection.getNetworkManager().getRemoteAddress();
                if (addr instanceof InetSocketAddress inet) {
                    host = inet.getHostString();
                    port = inet.getPort();
                }
            }
        } catch (Exception ignored) {}

        BotConnection bot = new BotConnection(name);
        bots.put(name, bot);
        bot.connect(host, port);
        return bot;
    }

    public void disconnect(String name) {
        BotConnection bot = bots.get(name);
        if (bot != null) {
            bot.disconnect();
            bots.remove(name);
        }
    }

    public void disconnectAll() {
        bots.values().forEach(BotConnection::disconnect);
        bots.clear();
    }

    public BotConnection get(String name) {
        return bots.get(name);
    }

    public Collection<BotConnection> getAll() {
        return bots.values();
    }

    public void tick() {
        for (BotConnection bot : bots.values()) {
            try {
                bot.tick();
                if (bot.isConnected() && bot.isControlled() && bot.getPlayHandler() != null) {
                    bot.getPlayHandler().tickMovement();
                }
            } catch (Exception ignored) {}
        }
    }
}
