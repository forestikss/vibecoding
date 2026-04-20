package ru.etc1337.client.commands;

import lombok.Getter;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.game.EventSendMessage;

import ru.etc1337.client.commands.impl.*;
import ru.etc1337.client.commands.impl.BotCommand;
import ru.etc1337.protection.interfaces.Include;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class CommandManager implements EventListener {
    private final List<Command> commands = new ArrayList<>();

    public List<Command> getCommands() { return commands; }

    public CommandManager() {
        Client.getEventManager().register(this);
    }

    @Include
    public void init() {
        register(new HelpCommand());
        register(new ConfigCommand());
        register(new VClipCommand());
        register(new FriendCommand());
        register(new ParseCommand());
        register(new GpsCommand());
        register(new BindCommand());
        register(new StaffCommand());
        register(new TargetCommand());
        register(new MacroCommand());
        register(new BotCommand());
    }

    public CommandManager start() {
        init();
        return this;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventSendMessage eventSendMessage) {
            String message = eventSendMessage.getMessage();
            String prefix = ".";
            if (message.startsWith(prefix)) {

                message = message.substring(1);
                final String[] args = message.split(" ");

                for (Command command : this.getCommands()) {
                    if (Arrays.stream(command.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(args[0]))) {
                        command.execute(args);
                    }
                }

                event.setCancelled(true);
            }
        }
    }

    private void register(Command command) {
        getCommands().add(command);
    }
   
}
