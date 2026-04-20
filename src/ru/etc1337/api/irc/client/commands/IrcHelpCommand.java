package ru.etc1337.api.irc.client.commands;

import ru.etc1337.api.irc.client.IRCClient;
import ru.etc1337.client.commands.api.CommandInfo;

@CommandInfo(name = "help", aliases = {"?"}, description = "Помощь по IRC командам")
public class IrcHelpCommand extends IrcCommand {
    public IrcHelpCommand(IRCClient ircClient) { super(ircClient); }

    @Override
    public void execute(String[] args) {
        ircClient.sendCommand("#help");
    }
}