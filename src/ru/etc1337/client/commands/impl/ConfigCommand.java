package ru.etc1337.client.commands.impl;

import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.config.Config;
import ru.etc1337.api.config.ConfigManager;
import ru.etc1337.api.game.Chat;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.commands.api.CommandParameter;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.io.IOException;

@CommandInfo(name = "Config", description = "Позволяет загрузить/удалить/добавить конфиг", aliases = {"config", "cfg"})
public class ConfigCommand extends Command {

    CommandParameter add = new CommandParameter(this, "add", "save", "фвв", "ыфму");
    CommandParameter load = new CommandParameter(this, "load", "дщфв");
    CommandParameter remove = new CommandParameter(this, "remove", "куьщму", "delete", "del", "вудуеу");
    CommandParameter list = new CommandParameter(this, "list", "дшые", "список");
    CommandParameter dir = new CommandParameter(this, "dir", "вшк", "папка");
    @Compile
    @Override
    public void execute(String[] args) {
        if (args.length > 1) {
            ConfigManager configManager = Client.getInstance().getConfigManager();
            if (this.contains(args[1], add)) {
                if (args.length < 3) {
                    sendErrorMessage();
                    return;
                }
                String configName = args[2];

                Config config = configManager.getConfig(configName);
                if (config == null) {
                    config = new Config(configName);
                }
                config.save();
                Chat.send("Config with name " + TextFormatting.AQUA + configName + TextFormatting.RESET + " was saved");
            } else if (this.contains(args[1], load)) {
                if (args.length < 3) {
                    sendErrorMessage();
                    return;
                }
                String configName = args[2];

                Config config = configManager.getConfig(configName);
                if (config != null) {
                    configManager.getConfig(configName).start();
                } else {
                    Chat.send("Config with name " + TextFormatting.AQUA + args[2] + TextFormatting.RESET + " does not exist");
                }
            } else if (this.contains(args[1], remove)) {
                if (args.length < 3) {
                    sendErrorMessage();
                    return;
                }
                String configName = args[2];

                Config config = configManager.getConfig(configName);
                if (config != null) {
                    configManager.deleteConfig(config);
                    Chat.send("Config with name " + TextFormatting.AQUA + args[2] + TextFormatting.RESET + " was removed");
                } else {
                    Chat.send("Config with name " + TextFormatting.AQUA + args[2] + TextFormatting.RESET + " does not exist");
                }
            } else if (this.contains(args[1], list)) {
                Chat.send("Configs: ");
                for (String configName : configManager.getConfigs()) {
                    Chat.send(TextFormatting.GRAY + String.valueOf(TextFormatting.BOLD) + "> " + TextFormatting.AQUA + configName);
                }
            } else if (this.contains(args[1], dir)) {
                try {
                    Runtime.getRuntime().exec("explorer " + Config.DIRECTORY.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                sendErrorMessage();
            }
        } else {
            sendErrorMessage();
        }
    }
    @Compile
    public void sendErrorMessage() {
        error();
        Chat.send(TextFormatting.GRAY + ".config save " + TextFormatting.AQUA + "<name>");
        Chat.send(TextFormatting.GRAY + ".config load " + TextFormatting.AQUA + "<name>");
        Chat.send(TextFormatting.GRAY + ".config remove/del/delete " + TextFormatting.AQUA + "<name>");
        Chat.send(TextFormatting.GRAY + ".config list");
        Chat.send(TextFormatting.GRAY + ".config clear");
        Chat.send(TextFormatting.GRAY + ".config dir");
    }
    
}
