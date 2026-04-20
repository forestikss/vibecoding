package ru.etc1337.client.commands.impl;


import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.game.Chat;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.commands.api.CommandParameter;

@CommandInfo(name = "Target", description = "Позволяет добавить тарегтов, чтобы их таргетил чит", aliases = {"t", "target", "targets"})
public class TargetCommand extends Command {

    CommandParameter add = new CommandParameter(this, "add", "save", "фвв", "ыфму");
    CommandParameter remove = new CommandParameter(this, "remove", "куьщму", "delete", "del", "вудуеу");
    CommandParameter list = new CommandParameter(this, "list", "дшые", "список");
    CommandParameter clear = new CommandParameter(this, "clear", "сдуфк");

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendErrorMessage();
            return;
        }
        if (this.contains(args[1], add)) {
            if (args.length == 3) {
                if (!Client.getInstance().getTargetManager().isTarget(args[2])) {
                    addTarget(args[2]);
                } else {
                    Chat.send("Player " + args[2] + " is already in the targets list.");
                }
            } else {
                sendErrorMessage();
            }
        } else if (this.contains(args[1], remove)) {
            if (args.length == 3) {
                if (Client.getInstance().getTargetManager().isTarget(args[2])) {
                    removeTarget(args[2]);
                } else {
                    Chat.send("Player " + args[2] + " is not in the targets list.");
                }
            } else {
                sendErrorMessage();
            }
        } else if (this.contains(args[1], list)) {
            displayTargets();
        } else if (this.contains(args[1], clear)) {
            clearTargets();
        } else {
            sendErrorMessage();
        }
    }

    public void addTarget(String name) {
        Client.getInstance().getTargetManager().addTarget(name);
        Chat.send("Success! " + name + " was added as a targets!");
    }

    public void removeTarget(String name) {
        Client.getInstance().getTargetManager().removeTarget(name);
        Chat.send("Success! " + name + " was removed from targets!");
    }

    public void displayTargets() {
        if (Client.getInstance().getTargetManager().getTargets().isEmpty()) {
            Chat.send("targets list is empty.");
        } else {
            Chat.send("targets: ");
            for (String target : Client.getInstance().getTargetManager().getTargets()) {
                Chat.send(TextFormatting.GRAY + String.valueOf(TextFormatting.BOLD) + "> " + TextFormatting.WHITE + target);
            }
        }
    }

    public void clearTargets() {
        Client.getInstance().getTargetManager().getTargets().clear();
        Chat.send("Success! The target list has been cleared!");
        
    }

    public void sendErrorMessage() {
        error();
        Chat.send(TextFormatting.GRAY + ".target " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".target add " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".target remove/del/delete " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".target list");
        Chat.send(TextFormatting.GRAY + ".target clear");
    }
}