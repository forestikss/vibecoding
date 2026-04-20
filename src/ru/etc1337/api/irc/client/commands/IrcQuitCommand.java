package ru.etc1337.api.irc.client.commands;

import ru.etc1337.api.irc.client.IRCClient;
import ru.etc1337.client.commands.api.CommandInfo;

@CommandInfo(name = "quit", aliases = {"disconnect", "exit"}, description = "Отключиться от IRC")
public class IrcQuitCommand extends IrcCommand {
    public IrcQuitCommand(IRCClient ircClient) { super(ircClient); }

    @Override
    public void execute(String[] args) {
        ircClient.sendCommand("#quit");
    }
}