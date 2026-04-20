package ru.etc1337.api.irc.client.commands;

import ru.etc1337.api.irc.client.IRCClient;
import ru.etc1337.client.commands.api.CommandInfo;

@CommandInfo(name = "nick", aliases = {}, description = "Установить/сменить ник")
public class IrcNickCommand extends IrcCommand {
    public IrcNickCommand(IRCClient ircClient) { super(ircClient); }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            ircClient.printToChat("[IRC] Использование: #nick <новое_имя>");
            return;
        }
        ircClient.sendCommand("#nick " + args[0]);
    }
}