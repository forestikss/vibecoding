package ru.etc1337.api.discord.rpc;

import ru.etc1337.Client;
import ru.etc1337.api.discord.rpc.utils.DiscordEventHandlers;
import ru.etc1337.api.discord.rpc.utils.DiscordRPC;
import ru.etc1337.api.discord.rpc.utils.DiscordRichPresence;
import ru.etc1337.api.discord.rpc.utils.RPCButton;
import lombok.Getter;
import ru.etc1337.protection.interfaces.Include;

@Getter
public class DiscordManager {

    private DiscordDaemonThread discordDaemonThread;
    private long APPLICATION_ID;

    private boolean running;

    private String image;
    private String telegram;
    private String site;

    @Include
    private void cppInit() {
        discordDaemonThread = new DiscordDaemonThread();
        APPLICATION_ID = 1298582136105996319L;
        running = true;
        image = "https://i.ibb.co/Rkjpbf5t/avatar.png";
        telegram = "https://t.me/dreamcore_dlc/";
        site = "https://dreamcore.fun";
    }

    @Include
    public void init() {
        cppInit();
        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().build();
        DiscordRPC.INSTANCE.Discord_Initialize(String.valueOf(APPLICATION_ID), handlers, true, "");
        builder.setStartTimestamp((System.currentTimeMillis() / 1000));
        String username = Client.getInstance().getUserInfo().getUsername();
        String role = Client.getInstance().getUserInfo().getRole();
        //   String uid = String.valueOf(Excellent.getInst().getProfile().getId());
        builder.setDetails("User: " + username);
        builder.setState("Role: " + role);
        builder.setLargeImage(image, "Stradix.cc");
        builder.setButtons(RPCButton.create("Telegram", telegram), RPCButton.create("Site", site));
        DiscordRPC.INSTANCE.Discord_UpdatePresence(builder.build());
        discordDaemonThread.start();

    }


    public DiscordManager start() {
        init();
        return this;
    }



    public void stopRPC() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
        discordDaemonThread.interrupt();
        this.running = false;
    }

    private class DiscordDaemonThread extends Thread {
        @Override
        public void run() {
            this.setName("Discord-RPC");

            try {
                while (Client.getInstance().getDiscordManager().isRunning()) {
                    DiscordRPC.INSTANCE.Discord_RunCallbacks();
                    Thread.sleep(15 * 1000);
                }
            } catch (Exception exception) {
                stopRPC();
            }

            super.run();
        }
    }
}