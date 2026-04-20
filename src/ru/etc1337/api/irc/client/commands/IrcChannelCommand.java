package ru.etc1337.api.irc.client.commands;

import ru.etc1337.api.irc.client.IRCClient;
import ru.etc1337.client.commands.api.CommandInfo;

@CommandInfo(name = "channel", aliases = {"ch"}, description = "Управление каналами")
public class IrcChannelCommand extends IrcCommand {
    public IrcChannelCommand(IRCClient ircClient) { super(ircClient); }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ircClient.printToChat("[IRC] Использование: #channel <create|join|leave> [аргументы...]");
            return;
        }
        ircClient.sendCommand("#channel " + joinArgs(args));
    }
}