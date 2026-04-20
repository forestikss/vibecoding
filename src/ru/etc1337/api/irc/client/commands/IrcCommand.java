package ru.etc1337.api.irc.client.commands;

import lombok.Getter;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.api.irc.client.IRCClient;

import java.util.Arrays;

@Getter
public abstract class IrcCommand {
    private final String name;
    private final String[] aliases;
    private final String description;
    protected final IRCClient ircClient;

    public IrcCommand(IRCClient ircClient) {
        CommandInfo annotation = getClass().getAnnotation(CommandInfo.class);
        if (annotation == null) {
            throw new IllegalStateException("Класс команды " + getClass().getSimpleName() + " должен иметь аннотацию @CommandInfo");
        }
        this.name = annotation.name();
        this.aliases = annotation.aliases();
        this.description = annotation.description();
        this.ircClient = ircClient;
    }

    public abstract void execute(String[] args);

    protected String joinArgs(String[] args) {
        return String.join(" ", args);
    }

    protected String joinArgsFrom(int index, String[] args) {
        if (index >= args.length) return "";
        return String.join(" ", Arrays.copyOfRange(args, index, args.length));
    }
}