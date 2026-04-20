package ru.etc1337.client.commands.impl;

import ru.etc1337.Client;
import ru.etc1337.api.bot.BotConnection;
import ru.etc1337.api.bot.BotManager;
import ru.etc1337.api.game.Chat;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.modules.impl.misc.Bots;

@CommandInfo(name = "Bot", aliases = {"bot"}, description = "Управление ботами")
public class BotCommand extends Command {

    @Override
    public void execute(String[] args) {
        Bots botsModule = Client.getInstance().getModuleManager().get(Bots.class);
        if (botsModule == null || !botsModule.isEnabled()) {
            Chat.send("§cВключи модуль §eBots§c!");
            return;
        }
        BotManager manager = botsModule.getBotManager();

        if (args.length < 2) {
            sendHelp();
            return;
        }

        switch (args[1].toLowerCase()) {

            // .bot connect <ник>
            case "connect" -> {
                if (args.length < 3) { error(".bot connect <ник>"); return; }
                String name = args[2];
                manager.connect(name);
                Chat.send("§aБот §e" + name + " §aподключается...");
            }

            // .bot disconnect <ник|all>
            case "disconnect", "dc" -> {
                if (args.length < 3) { error(".bot disconnect <ник|all>"); return; }
                if (args[2].equalsIgnoreCase("all")) {
                    manager.disconnectAll();
                    Chat.send("§cВсе боты отключены.");
                } else {
                    manager.disconnect(args[2]);
                    Chat.send("§cБот §e" + args[2] + " §cотключён.");
                }
            }

            // .bot chat <ник|all> <текст>
            case "chat" -> {
                if (args.length < 4) { error(".bot chat <ник|all> <текст>"); return; }
                String target = args[2];
                String msg = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                if (target.equalsIgnoreCase("all")) {
                    for (BotConnection bot : manager.getAll()) bot.sendChat(msg);
                    Chat.send("§7[All bots] §f" + msg);
                } else {
                    BotConnection bot = manager.get(target);
                    if (bot == null) { Chat.send("§cБот §e" + target + " §cне найден."); return; }
                    bot.sendChat(msg);
                    Chat.send("§7[" + target + "] §f" + msg);
                }
            }

            // .bot control <ник>  — начать контроль
            case "control" -> {
                if (args.length < 3) { error(".bot control <ник>"); return; }
                BotConnection bot = manager.get(args[2]);
                if (bot == null) { Chat.send("§cБот не найден."); return; }
                // Снимаем контроль со всех остальных
                manager.getAll().forEach(b -> b.setControlled(false));
                bot.setControlled(true);
                // Открываем экран управления
                mc.displayGuiScreen(new ru.etc1337.api.bot.BotControlScreen(bot));
                Chat.send("§aКонтроль над §e" + args[2] + " §aактивирован.");
            }

            // .bot return  — убрать контроль
            case "return" -> {
                manager.getAll().forEach(b -> b.setControlled(false));
                if (mc.currentScreen instanceof ru.etc1337.api.bot.BotControlScreen) {
                    mc.displayGuiScreen(null);
                }
                Chat.send("§7Контроль снят.");
            }

            // .bot list
            case "list" -> {
                if (manager.getAll().isEmpty()) {
                    Chat.send("§7Нет активных ботов.");
                } else {
                    Chat.send("§7Боты:");
                    for (BotConnection bot : manager.getAll()) {
                        String status = bot.isConnected() ? "§aOnline" : "§cOffline";
                        String ctrl = bot.isControlled() ? " §e[Control]" : "";
                        Chat.send("  §f" + bot.getName() + " " + status + ctrl);
                    }
                }
            }

            default -> sendHelp();
        }
    }

    private void sendHelp() {
        Chat.send("§7.bot connect §f<ник>");
        Chat.send("§7.bot disconnect §f<ник|all>");
        Chat.send("§7.bot chat §f<ник|all> <текст>");
        Chat.send("§7.bot control §f<ник>");
        Chat.send("§7.bot return");
        Chat.send("§7.bot list");
    }
}
