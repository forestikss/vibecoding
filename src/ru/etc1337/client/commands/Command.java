package ru.etc1337.client.commands;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.commands.api.CommandParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public abstract class Command implements QuickImports {

    private final String displayName, description;
    private final String[] aliases;
    private final CommandInfo annotation = this.getClass().getAnnotation(CommandInfo.class);

    @Getter
    private final List<CommandParameter> parameters = new ArrayList<>();

    public Command() {
        this.displayName = annotation.name();
        this.description = annotation.description();
        this.aliases = annotation.aliases();
    }

    public abstract void execute(String[] args);

    protected boolean contains(String arg, String... texts) {
        return Arrays.asList(texts).contains(arg.toLowerCase());
    }

    protected boolean contains(String arg, CommandParameter cmd) {
        return Arrays.asList(cmd.getNames()).contains(arg.toLowerCase());
    }

    protected final void error() {
        Chat.sendError("Invalid command usage.");
    }

    protected final void error(String usage) {
        error();
        Chat.send("Correct Usage: " + usage);
    }
}