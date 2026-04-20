package ru.etc1337.api.bot;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import ru.etc1337.api.interfaces.QuickImports;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BotConnection implements QuickImports {

    private final String name;
    private NetworkManager networkManager;
    private BotPlayHandler playHandler;
    private boolean connected = false;
    private boolean controlled = false;
    private boolean showChat = false;
    private boolean spoofResourcePack = false;

    private final ConcurrentLinkedQueue<String> chatMessages = new ConcurrentLinkedQueue<>();

    public BotConnection(String name) { this.name = name; }

    // Геттеры
    public String getName()                              { return name; }
    public NetworkManager getNetworkManager()            { return networkManager; }
    public BotPlayHandler getPlayHandler()               { return playHandler; }
    public boolean isConnected()                         { return connected; }
    public boolean isControlled()                        { return controlled; }
    public boolean isShowChat()                          { return showChat; }
    public boolean isSpoofResourcePack()                 { return spoofResourcePack; }
    public ConcurrentLinkedQueue<String> getChatMessages() { return chatMessages; }

    // Сеттеры
    public void setConnected(boolean v)        { this.connected = v; }
    public void setControlled(boolean v)       { this.controlled = v; }
    public void setShowChat(boolean v)         { this.showChat = v; }
    public void setSpoofResourcePack(boolean v){ this.spoofResourcePack = v; }
    public void setPlayHandler(BotPlayHandler h){ this.playHandler = h; }

    public void connect(String host, int port) {
        Thread thread = new Thread(() -> {
            try {
                ServerAddress addr = ServerAddress.fromString(host + ":" + port);
                InetAddress inetAddr = InetAddress.getByName(addr.getIP());

                networkManager = NetworkManager.createNetworkManagerAndConnect(
                        inetAddr, addr.getPort(), true);

                networkManager.setNetHandler(new BotLoginHandler(this, networkManager));
                networkManager.sendPacket(new CHandshakePacket(addr.getIP(), addr.getPort(), net.minecraft.network.ProtocolType.LOGIN));
                networkManager.sendPacket(new CLoginStartPacket(
                        new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()), name)));

            } catch (Exception e) {
                chatMessages.add("[Bot:" + name + "] Ошибка: " + e.getMessage());
            }
        }, "Bot-" + name);
        thread.setDaemon(true);
        thread.start();
    }

    public void disconnect() {
        if (networkManager != null && networkManager.isChannelOpen())
            networkManager.closeChannel(null);
        connected = false;
        controlled = false;
    }

    public void sendChat(String message) {
        if (playHandler != null && connected) playHandler.sendChat(message);
    }

    public void tick() {
        if (networkManager != null && networkManager.isChannelOpen())
            networkManager.tick();
    }

    public void addChatMessage(String msg) {
        if (showChat) chatMessages.add("[" + name + "] " + msg);
    }

    /** Системное сообщение — добавляется всегда */
    public void addSystemMessage(String msg) {
        chatMessages.add(msg);
    }
}
