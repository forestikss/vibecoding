package ru.etc1337.api.irc.client.commands;

import ru.etc1337.api.irc.client.IRCClient;
import ru.etc1337.client.commands.api.CommandInfo;

@CommandInfo(name = "message", aliases = {"say"}, description = "Отправить сообщение в текущий канал")
public class IrcMessageCommand extends IrcCommand {
    public IrcMessageCommand(IRCClient ircClient) { super(ircClient); }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) return;
        ircClient.sendCommand("#message " + joinArgs(args));
    }
}