package ru.etc1337.client.commands.impl;

import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.game.Chat;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.commands.api.CommandParameter;

@CommandInfo(name = "Staff", aliases = {"staffs", "staff"}, description = "Позволяет добавлять/удалять администрацию сервера из списка")
public class StaffCommand extends Command {

    CommandParameter add = new CommandParameter(this, "add");
    CommandParameter remove = new CommandParameter(this, "remove", "del", "delete");
    CommandParameter list = new CommandParameter(this, "list");
    CommandParameter clear = new CommandParameter(this, "clear");

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendErrorMessage();
            return;
        }

        if (this.contains(args[1], add)) {
            if (args.length == 3) {
                addStaff(args[2]);
            } else {
                sendErrorMessage();
            }
        } else if (this.contains(args[1], remove)) {
            if (args.length == 3) {
                removeStaff(args[2]);
            } else {
                sendErrorMessage();
            }
        } else if (this.contains(args[1], list)) {
            displayStaff();
        } else if (this.contains(args[1], clear)) {
            clearStaff();
        } else {
            sendErrorMessage();
        }
    }

    public void addStaff(String name) {

        if (Client.getInstance().getStaffManager().contains(name)) {
            Chat.send("Staff with name %s already exists.".formatted(name));
        } else {
            Client.getInstance().getStaffManager().addStaff(name);
            Chat.send("Staff with name %s was added.".formatted(name));
            
        }
    }

    public void removeStaff(String name) {
        if (Client.getInstance().getStaffManager().contains(name)) {
            Client.getInstance().getStaffManager().removeStaff(name);
            Chat.send("Staff with name %s was removed.".formatted(name));
            
        } else {
            Chat.send("Staff with name %s does not exist.".formatted(name));
        }
    }

    public void clearStaff() {
        Client.getInstance().getStaffManager().getStaff().clear();
        Chat.send("Staff list was cleared.");
        
    }

    public void displayStaff() {
        if (Client.getInstance().getStaffManager().getStaff().isEmpty()) {
            Chat.send("Staff list is empty :(");
        } else {
            Chat.send("Staff: ");
            for (String staff : Client.getInstance().getStaffManager().getStaff()) {
                Chat.send(TextFormatting.GRAY + String.valueOf(TextFormatting.BOLD) + "> " + TextFormatting.WHITE + staff);
            }
        }
    }

    public void sendErrorMessage() {
        error();
        Chat.send(TextFormatting.GRAY + ".staff add " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".staff remove/del/delete " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".staff list");
        Chat.send(TextFormatting.GRAY + ".staff clear");
    }
}

