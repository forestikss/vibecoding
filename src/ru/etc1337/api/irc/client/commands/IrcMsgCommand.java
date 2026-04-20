package ru.etc1337.api.irc.client.commands;

import ru.etc1337.api.irc.client.IRCClient;
import ru.etc1337.client.commands.api.CommandInfo;

@CommandInfo(name = "msg", aliases = {"m", "tell", "w"}, description = "Отправить личное сообщение")
public class IrcMsgCommand extends IrcCommand {
    public IrcMsgCommand(IRCClient ircClient) { super(ircClient); }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ircClient.printToChat("[IRC] Использование: #msg <ник> <сообщение>");
            return;
        }
        ircClient.sendCommand("#msg " + args[0] + " " + joinArgsFrom(1, args));
    }
}