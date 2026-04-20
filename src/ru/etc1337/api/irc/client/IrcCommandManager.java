package ru.etc1337.api.irc.client;

import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.api.irc.client.commands.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class IrcCommandManager {
    private final List<IrcCommand> commands = new ArrayList<>();
    private final IRCClient ircClient;

    public IrcCommandManager(IRCClient ircClient) {
        this.ircClient = ircClient;
        registerCommands();
    }

    private void registerCommands() {
        commands.add(new IrcChannelCommand(ircClient));
        commands.add(new IrcHelpCommand(ircClient));
        commands.add(new IrcMessageCommand(ircClient));
        commands.add(new IrcMsgCommand(ircClient));
        commands.add(new IrcNickCommand(ircClient));
        commands.add(new IrcQuitCommand(ircClient));
    }

    public void handleCommand(String rawMessage) {
        String message = rawMessage.substring(1); // Убираем '#'
        String[] parts = message.split("\\s+");
        String commandName = parts[0].toLowerCase();
        
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        for (IrcCommand command : commands) {
            if (command.getName().equalsIgnoreCase(commandName) || 
                Arrays.stream(command.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(commandName))) {
                command.execute(args);
                return;
            }
        }
        
        // Если команда не найдена, считаем это сообщением в канал
        ircClient.sendCommand("#message " + message);
    }
}