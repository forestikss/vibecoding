package ru.etc1337.client.commands.api;

import lombok.Getter;
import ru.etc1337.client.commands.Command;

@Getter
public class CommandParameter {
    private final Command command;
    private final String[] names;
    
    public CommandParameter(Command cmd, String... names) {
        this.command = cmd;
        this.names = names;
        
        cmd.getParameters().add(this);
        
        if (names.length == 0) {
            throw new IllegalArgumentException("Parameter must have at least one name");
        }
    }
    
    public String getPrimaryName() {
        return names[0];
    }
}