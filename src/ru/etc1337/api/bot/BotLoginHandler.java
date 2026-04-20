package ru.etc1337.api.bot;

import net.minecraft.client.network.login.IClientLoginNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.login.server.*;
import net.minecraft.util.text.ITextComponent;

public class BotLoginHandler implements IClientLoginNetHandler {

    private final BotConnection bot;
    private final NetworkManager networkManager;

    public BotLoginHandler(BotConnection bot, NetworkManager networkManager) {
        this.bot = bot;
        this.networkManager = networkManager;
    }

    public BotConnection getBot() { return bot; }

    @Override
    public void handleLoginSuccess(SLoginSuccessPacket packet) {
        networkManager.setConnectionState(ProtocolType.PLAY);
        BotPlayHandler playHandler = new BotPlayHandler(bot, networkManager);
        networkManager.setNetHandler(playHandler);
        bot.setPlayHandler(playHandler);
        bot.setConnected(true);
        bot.addSystemMessage("§aПодключился к серверу");
    }

    @Override
    public void handleDisconnect(SDisconnectLoginPacket packet) {
        bot.setConnected(false);
        bot.addSystemMessage("§cОтключён при логине: " + packet.getReason().getString());
    }

    @Override
    public void handleEncryptionRequest(SEncryptionRequestPacket packet) {
        // Офлайн-режим — игнорируем
    }

    @Override
    public void handleEnableCompression(SEnableCompressionPacket packet) {
        // Компрессия — игнорируем для простоты
    }

    @Override
    public void handleCustomPayloadLogin(SCustomPayloadLoginPacket packet) {
        // Отвечаем что не поддерживаем
    }

    @Override
    public NetworkManager getNetworkManager() { return networkManager; }

    @Override
    public void onDisconnect(ITextComponent reason) {
        bot.setConnected(false);
        bot.addChatMessage("§cОтключён: " + reason.getString());
    }
}
