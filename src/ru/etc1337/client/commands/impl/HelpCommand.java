package ru.etc1337.client.commands.impl;

import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.game.Chat;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@CommandInfo(name = "help", description = "Показывает список доступных команд клиента", aliases = {"help"})
public class HelpCommand extends Command {
    @Compile
    @Override
    public void execute(String[] args) {
        for (Command command : Client.getInstance().getCommandManager().getCommands()) {
            if (command == this) continue;
            Chat.send(TextFormatting.GRAY + String.valueOf(TextFormatting.BOLD) + "> " + TextFormatting.RESET + command.getDisplayName() + " - " + command.getDescription());
        }
    }
}
